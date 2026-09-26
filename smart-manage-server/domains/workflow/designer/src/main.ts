import { createApp, h } from 'vue';
import { createPinia } from 'pinia';
import { FlowDesigner, WarmFlowDesigner, setDataProvider, setUiAdapter } from '@warm-flow/designer';
import { elementPlusAdapter } from '@warm-flow/element-plus';
import 'element-plus/dist/index.css';
import './style.css';
import { designerDefinition, feedbackPayload } from './protocol.mjs';

const query = new URLSearchParams(location.search);
const definitionId = query.get('definitionId');
const instanceId = query.get('instanceId');
const readOnly = Boolean(instanceId) || query.get('readOnly') === 'true';
const apiRoot = new URL('../process/', location.href);
let csrfToken: string | null = null;
let digest: string | null = null;

async function request(path: string, payload?: unknown) {
  const endpoint = new URL(path, apiRoot);
  // 数据接口固定在同源工作流目录，父页面和 URL 均不能指定任意请求目标。
  if (endpoint.origin !== location.origin || !endpoint.pathname.startsWith(apiRoot.pathname)) {
    throw new Error('非法设计器请求');
  }
  if (payload !== undefined && !csrfToken) {
    const sessionResponse = await fetch(new URL('../../sys/base/session', location.href), { credentials: 'same-origin' });
    const session = await sessionResponse.json();
    if (!sessionResponse.ok || session.code !== 0 || typeof session.data?.csrfToken !== 'string') {
      throw new Error('登录状态已失效，请重新登录');
    }
    csrfToken = session.data.csrfToken;
  }
  const response = await fetch(endpoint, {
    method: payload === undefined ? 'GET' : 'POST',
    credentials: 'same-origin',
    headers: payload === undefined ? {} : { 'Content-Type': 'application/json', 'sm-csrf-token': csrfToken! },
    body: payload === undefined ? undefined : JSON.stringify(payload),
  });
  const result = await response.json();
  if (!response.ok || result.code !== 0) {
    if (result.code === 100419) csrfToken = null;
    throw new Error(typeof result.msg === 'string' ? result.msg : '工作流请求失败');
  }
  return { code: 200, msg: result.msg, data: result.data };
}

function emptyResult() { return Promise.resolve({ code: 200, data: [] }); }
async function queryDefinition() {
  const response = await request('definition/design/' + encodeURIComponent(definitionId ?? ''));
  digest = response.data.digest;
  return { ...response, data: designerDefinition(response.data.definition) };
}
async function queryChart(instanceId: string) {
  const response = await request('instance/chart/' + encodeURIComponent(instanceId));
  return { ...response, data: designerDefinition(response.data) };
}
function sendEvent(type: string, detail: unknown = null) {
  if (window.parent !== window) window.parent.postMessage({ channel: 'smart-manage-workflow', type, definitionId, detail }, location.origin);
}
// 覆盖完整公开数据契约，禁止回落到上游 URL/localStorage Token 请求层。
setDataProvider({
  queryDef: queryDefinition,
  saveJson: async (raw: unknown) => {
    if (readOnly) throw new Error('当前定义只读');
    const definition = designerDefinition(typeof raw === 'string' ? JSON.parse(raw) : raw);
    const response = await request('definition/design/save', { definitionId, definition, digest });
    digest = response.data.digest;
    return { ...response, data: definitionId };
  },
  queryFlowChart: queryChart,
  handlerType: () => request('assignment/types'),
  handlerResult: (parameters: unknown) => request('assignment/candidates', parameters),
  handlerFeedback: (parameters: unknown) => request('assignment/feedback', feedbackPayload(parameters)),
  handlerDict: () => request('assignment/rules'),
  publishedList: emptyResult,
  nodeExt: emptyResult,
  listenerList: emptyResult,
  config: () => Promise.resolve({ code: 200, data: { tokenNameList: [], framework: 'springboot' } }),
});
setUiAdapter(elementPlusAdapter);
async function mount() {
// 在挂载前完成加载，错误直接显示；避免官方内部 catch 将认证或接口失败伪装成空画布。
const initialJson = instanceId ? (await queryChart(instanceId)).data : (await queryDefinition()).data;
const application = createApp({
  render: () => h(FlowDesigner, {
    definitionId,
    initialJson,
    disabled: readOnly,
    onlyDesignShow: true,
    paletteNodes: { gatewayNodes: [{ type: 'serial', label: '条件分支', text: '条件分支' }] },
    onDirty: (dirty: boolean) => sendEvent('dirty', dirty),
    onSaved: () => sendEvent('saved'),
    onClose: () => sendEvent('close'),
    onReady: () => sendEvent('ready'),
  }),
});
application.use(createPinia());
application.use(WarmFlowDesigner);
application.mount('#app');
}
void mount().catch((error: unknown) => {
  const target = document.getElementById('app');
  if (target) target.textContent = error instanceof Error ? error.message : '流程图加载失败';
});
