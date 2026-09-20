import { createRoot } from 'react-dom/client';
import App from './App';
import { initializeIconCatalog } from './domain/common/component/iconCatalog';
import './styles/global.css';

const root = createRoot(document.getElementById('root')!);

async function startApplication() {
  root.render(
    <div className="sm-app-initializing" role="status">
      正在准备系统资源…
    </div>,
  );
  try {
    await initializeIconCatalog();
    root.render(<App />);
  } catch (error) {
    console.error('图标资源初始化失败', error);
    root.render(
      <div className="sm-app-initialization-error" role="alert">
        <strong>系统资源加载失败</strong>
        <span>请检查网络连接后重试</span>
        <button type="button" onClick={() => void startApplication()}>
          重新加载
        </button>
      </div>,
    );
  }
}

void startApplication();
