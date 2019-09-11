import {throttle} from 'lodash';
import {firstUpperTranslated} from '../../services/translationService';
import {FetchByPage, PagedResponse} from '../../state/domain-models/selections/selectionsModels';
import {SelectionListItem} from '../../state/user-selection/userSelectionModels';
import {CallbackWith, uuid} from '../../types/Types';
import {DropdownComponentProps} from './DropdownSelector';

const selectedOptions = (list: SelectionListItem[]): number =>
  list.filter((item: SelectionListItem) => item.selected).length;

export const replaceWhereId = (
  array: SelectionListItem[],
  newItem: SelectionListItem,
  id: uuid,
): SelectionListItem[] => {
  const index = array.findIndex((item: SelectionListItem) => item.id === id);
  return index === -1 ? array : ([...array.slice(0, index), newItem, ...array.slice(index + 1)]);
};

export const searchOverviewText = (list: SelectionListItem[], totalElements: number): string => {
  const numSelected: number = selectedOptions(list);
  return numSelected ? numSelected + ' / ' + totalElements : firstUpperTranslated('all');
};

export const unknownItems = ({selectedItems, unknownItem}: DropdownComponentProps): SelectionListItem[] => {
  const unknownIsSelected = unknownItem && selectedItems.find((item) => item.id === unknownItem.id);
  return unknownIsSelected === undefined && unknownItem !== undefined
    ? [unknownItem]
    : [];
};

export type ThrottledSearch<T> = (query: string, onResponse: CallbackWith<T>) => Promise<T>;

const asyncFunction = (onSearch: FetchByPage): ThrottledSearch<PagedResponse> =>
  async (query: string, onResponse: CallbackWith<PagedResponse>): Promise<any> => {
    if (query) {
      const {items, totalElements} = await onSearch(0, query);
      onResponse({items, totalElements, query});
    } else {
      onResponse({items: [], totalElements: 0, query});
    }
  };

export const throttledSearch = (onSearch: FetchByPage): ThrottledSearch<PagedResponse> =>
  throttle(asyncFunction(onSearch), 500, {leading: false, trailing: true});
