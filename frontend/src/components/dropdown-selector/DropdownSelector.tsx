import {default as classNames} from 'classnames';
import Popover from 'material-ui/Popover/Popover';
import PopoverAnimationVertical from 'material-ui/Popover/PopoverAnimationVertical';
import * as React from 'react';
import {Index, InfiniteLoader, List, ListRowProps} from 'react-virtualized';
import {dropdownStyle} from '../../app/themes';
import {getId} from '../../helpers/collections';
import {selectedFirstThenUnknownByNameAsc} from '../../helpers/comparators';
import {orUnknown} from '../../helpers/translations';
import {firstUpper} from '../../services/translationService';
import {Address, City} from '../../state/domain-models/location/locationModels';
import {FetchByPage, PagedResponse} from '../../state/domain-models/selections/selectionsApiActions';
import {SelectionListItem} from '../../state/user-selection/userSelectionModels';
import {Children, IdNamed, uuid} from '../../types/Types';
import {IconDropDown} from '../icons/IconDropDown';
import {Column} from '../layouts/column/Column';
import {Row, RowMiddle} from '../layouts/row/Row';
import {SearchBox} from '../search-box/SearchBox';
import {LabelWithSubtitle} from '../texts/Labels';
import {FirstUpper} from '../texts/Texts';
import {Checkbox} from './Checkbox';
import {replaceWhereId, searchOverviewText, throttledSearch, ThrottledSearch, unknownItems} from './dropdownHelper';
import './DropdownSelector.scss';
import origin = __MaterialUI.propTypes.origin;

type OptionalProps = Partial<{
  renderLabel: (index: number, items: SelectionListItem[]) => Children;
  rowHeight: number;
  visibleItems: number;
}>;

interface SelectableProps {
  fetchItems: FetchByPage;
  select: (props: SelectionListItem) => void;
  selectedItems: SelectionListItem[];
  selectionText: string;
}

type QueryProps = Partial<{
  fetchItemsByQuery: ThrottledSearch<PagedResponse>;
  unknownItem: SelectionListItem;
}>;

interface Cache {
  items: SelectionListItem[];
  totalElements: number;
}

interface State extends PagedResponse {
  anchorElement?: React.ReactInstance;
  cache: Cache;
  isOpen: boolean;
  isSearching: boolean;
  page: number;
}

const anchorOrigin: origin = {horizontal: 'left', vertical: 'bottom'};
const targetOrigin: origin = {horizontal: 'left', vertical: 'top'};

export type SearchableProps = QueryProps & SelectableProps & OptionalProps;
export type DropdownComponentProps = QueryProps & SelectableProps & Required<OptionalProps>;

const listItems = (
  selectedItems: SelectionListItem[],
  items: SelectionListItem[],
): SelectionListItem[] => {
  const selectedIds = selectedItems.map(getId);
  const unselected = items.map((item: SelectionListItem) => ({...item, selected: false}))
    .filter((item: SelectionListItem) => !selectedIds.includes(item.id));
  return [...selectedItems, ...unselected];
};

const withNewItems = (
  prevItems: SelectionListItem[],
  newItems: SelectionListItem[],
): SelectionListItem[] => {
  const selectedIds = prevItems.filter(it => it.selected).map(getId);
  const unselected = newItems.map(item => ({...item, selected: false}))
    .filter(item => !selectedIds.includes(item.id));
  return [...prevItems, ...unselected];
};

class DropdownComponent extends React.Component<DropdownComponentProps, State> {

  constructor(props: DropdownComponentProps) {
    super(props);
    const items = [...props.selectedItems, ...unknownItems(props)];
    this.state = {
      cache: {items, totalElements: 0},
      isOpen: false,
      isSearching: false,
      items,
      page: 0,
      totalElements: 0,
    };
  }

  async componentDidMount() {
    await this.loadMoreRows();
  }

  componentWillReceiveProps({selectedItems}: DropdownComponentProps) {
    if (!this.state.isOpen && this.props.selectedItems.length !== selectedItems.length) {
      this.setState(({items: prevItems, cache: {totalElements}}: State) => {
        const items = listItems(selectedItems, prevItems);
        return ({items, cache: {items, totalElements}});
      });
    }
  }

  render() {
    const {anchorElement, cache, isOpen, isSearching, items, totalElements, query} = this.state;
    const {fetchItemsByQuery, selectionText, selectedItems} = this.props;

    const selectedOverview: string = searchOverviewText(selectedItems, cache.totalElements);

    const numSelectedItems: number = selectedItems.length;
    const numItems: number = items.length;
    const offset: number = isSearching || !fetchItemsByQuery ? 0 : 1;

    const rowCount = (numItems - numSelectedItems + offset) < totalElements
      ? numItems + offset
      : totalElements;

    const renderSearchBox =
      fetchItemsByQuery && <SearchBox onChange={this.onUpdateSearch} clear={!isOpen} onClear={this.onClear}/>;

    return (
      <Row className="DropdownSelector">
        <div
          onClick={this.openMenu}
          className={classNames('DropdownSelector-Text clickable', {isOpen})}
        >
          <RowMiddle>
            <FirstUpper>{selectionText}{selectedOverview}</FirstUpper>
            <IconDropDown/>
          </RowMiddle>
        </div>

        <Popover
          style={dropdownStyle.popoverStyle}
          open={isOpen}
          anchorEl={anchorElement}
          anchorOrigin={anchorOrigin}
          targetOrigin={targetOrigin}
          onRequestClose={this.closeMenu}
          animation={PopoverAnimationVertical}
        >
          <Column className="DropdownSelector-menu">
            {renderSearchBox}
            <Row>
              <InfiniteLoader
                key={query}
                isRowLoaded={this.isRowLoaded}
                loadMoreRows={this.loadMoreRows}
                rowCount={rowCount}
              >
                {this.renderList}
              </InfiniteLoader>
            </Row>
          </Column>
        </Popover>
      </Row>
    );
  }

  openMenu = (event: any): void => {
    event.preventDefault();
    const {selectedItems} = this.props;
    this.setState({
        isOpen: true,
        anchorElement: event.currentTarget,
        items: listItems(selectedItems, this.state.cache.items).sort(selectedFirstThenUnknownByNameAsc),
      },
    );
  }

  closeMenu = (): void => this.setState({
    isOpen: false,
    isSearching: false,
    totalElements: this.state.cache.totalElements,
  })

  onUpdateSearch = (searchText: string) => {
    this.props.fetchItemsByQuery!(
      searchText,
      ({items, totalElements, query}: PagedResponse) => {
        if (query) {
          this.setState({
            items,
            isSearching: true,
            page: 1,
            totalElements,
            query,
          });
        } else {
          this.onClear();
        }
      },
    );
  }

  onClear = () => {
    const {selectedItems} = this.props;
    this.setState(({cache: {items, totalElements}}: State) => ({
      items: listItems(selectedItems, items).sort(selectedFirstThenUnknownByNameAsc),
      isSearching: false,
      page: 0,
      totalElements,
      query: undefined,
    }));
  }

  onSelect = (selectedItem: SelectionListItem, id: uuid) => {
    const newItem = {...selectedItem, selected: !selectedItem.selected};
    this.props.select(newItem);
    this.setState(({cache, items}) => ({items: replaceWhereId(items, newItem, id)}));
  }

  rowRenderer = ({index, style}: ListRowProps) => {
    const {items} = this.state;
    const selectedItem = items[index];
    const {id, selected} = selectedItem;
    const onClick = () => this.onSelect(selectedItem, id);
    const label = this.props.renderLabel(index, items)!;
    return (
      <Checkbox
        id={id}
        label={label}
        onClick={onClick}
        key={`${index}-${id}`}
        style={style}
        className="first-uppercase"
        checked={selected}
      />
    );
  }

  renderList = ({onRowsRendered, registerChild}) => {
    const numItems = this.state.items.length;
    const {visibleItems, rowHeight} = this.props;
    return (
      <List
        height={numItems > visibleItems ? visibleItems * rowHeight : numItems * rowHeight}
        onRowsRendered={onRowsRendered}
        rowHeight={rowHeight}
        ref={registerChild}
        rowCount={numItems}
        rowRenderer={this.rowRenderer}
        style={dropdownStyle.listStyle}
        width={240}
      />
    );
  }

  isRowLoaded = ({index}: Index): boolean => !!this.state.items[index];

  loadMoreRows = async (): Promise<SelectionListItem[] | {}> => {
    const {items: responseItems, totalElements} = await this.props.fetchItems(this.state.page);

    return new Promise((resolve) => {
      this.setState(({items: prevItems, page}: State) => {
        const items = withNewItems(prevItems, responseItems);
        return ({
          items,
          cache: {items, totalElements},
          totalElements,
          page: page + 1,
        });
      });
      return resolve(responseItems);
    });
  }

}

const translatedNameOf = ({name}: IdNamed): string => orUnknown(name);

const renderLabelAtIndex = (index: number, filteredList: SelectionListItem[]) => {
  const {name} = filteredList[index];
  return <FirstUpper>{name}</FirstUpper>;
};

export const renderCityLabel = (index: number, filteredList: SelectionListItem[]) => {
  const city = filteredList[index] as City;
  return <LabelWithSubtitle name={translatedNameOf(city)} subTitle={translatedNameOf(city.country)}/>;
};

export const renderAddressLabel = (index: number, filteredList: SelectionListItem[]) => {
  const address = filteredList[index] as Address;
  return (
    <LabelWithSubtitle
      name={translatedNameOf(address)}
      subTitle={`${firstUpper(translatedNameOf(address.city))}, ${firstUpper(translatedNameOf(address.country))}`}
    />
  );
};

const DropdownSelector = (props: SearchableProps) => (
  <DropdownComponent
    renderLabel={renderLabelAtIndex}
    rowHeight={40}
    visibleItems={10}
    {...props}
  />
);

export const SearchableDropdownSelector = (props: SearchableProps) => (
  <DropdownSelector
    fetchItemsByQuery={throttledSearch(props.fetchItems)}
    {...props}
  />
);

export const ListingDropdownSelector = (props: SelectableProps & OptionalProps) => (
  <DropdownSelector {...props}/>
);
