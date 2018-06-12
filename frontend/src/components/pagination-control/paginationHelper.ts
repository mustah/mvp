type PageLinkType = 'current' | 'link';
export type PageLink = [number, PageLinkType] | 'dot';

const withinRange = (search: number, range: number, proxy: number): boolean =>
  Math.abs(search - range) < proxy + 1;

export const pageLinks = (current: number, total: number, visibilityProxy: number): PageLink[] => {
  const pages: PageLink[] = [];
  let isLastPushedElementADot = false;
  for (let i = 0; i < total; i++) {
    if (i === current) {
      pages.push([i, 'current']);
      isLastPushedElementADot = false;
    } else if (withinRange(i, current, visibilityProxy) ||
      withinRange(i, 0, visibilityProxy) ||
      withinRange(i, total - 1, visibilityProxy)) {
      pages.push([i, 'link']);
      isLastPushedElementADot = false;
    } else if (!isLastPushedElementADot) {
      pages.push('dot');
      isLastPushedElementADot = true;
    }
  }
  return pages;
};
