import './ExternalLinkFrame.css';

interface Props {
  title: string;
  externalUrl: string;
}

/** 外链 iframe 保持普通浏览器能力，目标站点仍可通过自身 CSP/X-Frame-Options 拒绝嵌入。 */
const ExternalLinkFrame = ({ title, externalUrl }: Props) => (
  <iframe
    className="sm-external-link-frame"
    src={externalUrl}
    title={title}
    referrerPolicy="strict-origin-when-cross-origin"
    sandbox="allow-downloads allow-forms allow-modals allow-popups allow-popups-to-escape-sandbox allow-same-origin allow-scripts"
  />
);

export default ExternalLinkFrame;
