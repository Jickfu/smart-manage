import type { PropsWithChildren } from 'react';
import './HomeCardGrid.css';

interface HomeCardGridProps extends PropsWithChildren {
  className?: string;
}

const HomeCardGrid = ({ children, className }: HomeCardGridProps) => (
  <section className={`sm-home-card-grid${className ? ` ${className}` : ''}`}>{children}</section>
);

export default HomeCardGrid;
