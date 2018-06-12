import {pageLinks, PageLink} from '../paginationHelper';

describe('paginationHelper', () => {

  it('shows all numbers if there is only one', () => {
    const total: number = 1;
    const pages: PageLink[] = pageLinks(0, total, 0);
    expect(pages).toHaveLength(total);
  });

  it('finds the current page', () => {
    const pages: PageLink[] = pageLinks(0, 1, 0);
    const expected: PageLink = [0, 'current'];
    expect(pages[0]).toEqual(expected);
  });

  it('shows first and last, including visibility proxy', () => {
    const pages: PageLink[] = pageLinks(3, 10, 1);
    const expected: PageLink[] = [
      [0, 'link'],
      [1, 'link'], // proxy from first
      [2, 'link'], // proxy from current
      [3, 'current'],
      [4, 'link'],
      'dot',
      [8, 'link'], // proxy from last
      [9, 'link'],
    ];
    expect(pages).toEqual(expected);
  });

  it('shows pages within visibility proximity of current as link', () => {
    const pages: PageLink[] = pageLinks(2, 5, 1);
    const expected: PageLink[] = [
      [0, 'link'],
      [1, 'link'],
      [2, 'current'],
      [3, 'link'],
      [4, 'link'],
    ];
    expect(pages).toEqual(expected);
  });

  it('shows pages outside of proximity of current as dots', () => {
    const pages: PageLink[] = pageLinks(0, 4, 0);
    const expected: PageLink[] = [
      [0, 'current'],
      'dot',
      [3, 'link'],
    ];
    expect(pages).toEqual(expected);
  });

  it('lumps together dots', () => {
    const pages: PageLink[] = pageLinks(1, 10, 1);
    const expected: PageLink[] = [
      [0, 'link'],
      [1, 'current'],
      [2, 'link'],
      'dot',
      [8, 'link'],
      [9, 'link'],
    ];
    expect(pages).toEqual(expected);
  });

});
