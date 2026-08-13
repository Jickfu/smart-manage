import { useCallback, useLayoutEffect, useRef, useState } from 'react';

const SCROLL_DISTANCE = 240;

/** 管理页签横向滚动，并确保新激活的页签始终进入可视区域。 */
export function useHorizontalTabScroll(activeKey: string, tabCount: number) {
  const viewportRef = useRef<HTMLDivElement>(null);
  const activeTabRef = useRef<HTMLDivElement>(null);
  const [overflowing, setOverflowing] = useState(false);
  const [canScrollLeft, setCanScrollLeft] = useState(false);
  const [canScrollRight, setCanScrollRight] = useState(false);

  const updateScrollState = useCallback(() => {
    const viewport = viewportRef.current;
    if (!viewport) return;
    setOverflowing(viewport.scrollWidth > viewport.clientWidth + 1);
    setCanScrollLeft(viewport.scrollLeft > 1);
    setCanScrollRight(viewport.scrollLeft + viewport.clientWidth < viewport.scrollWidth - 1);
  }, []);

  useLayoutEffect(() => {
    const viewport = viewportRef.current;
    if (!viewport) return;

    activeTabRef.current?.scrollIntoView({
      behavior: 'smooth',
      block: 'nearest',
      inline: 'nearest',
    });
    updateScrollState();

    const resizeObserver = new ResizeObserver(updateScrollState);
    resizeObserver.observe(viewport);
    const content = viewport.firstElementChild;
    if (content) resizeObserver.observe(content);
    viewport.addEventListener('scroll', updateScrollState, { passive: true });

    return () => {
      resizeObserver.disconnect();
      viewport.removeEventListener('scroll', updateScrollState);
    };
  }, [activeKey, tabCount, updateScrollState]);

  const scroll = useCallback((direction: -1 | 1) => {
    viewportRef.current?.scrollBy({ left: direction * SCROLL_DISTANCE, behavior: 'smooth' });
  }, []);

  return { viewportRef, activeTabRef, overflowing, canScrollLeft, canScrollRight, scroll };
}
