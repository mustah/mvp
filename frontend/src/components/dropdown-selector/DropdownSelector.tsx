import * as classNames from 'classnames';
import Popover from 'material-ui/Popover/Popover';
import PopoverAnimationVertical from 'material-ui/Popover/PopoverAnimationVertical';
import * as React from 'react';
import {Index, InfiniteLoader, List, ListRowProps} from 'react-virtualized';
import {dropDownStyle} from '../../app/themes';
import {getId} from '../../helpers/collections';
import {selectedFirstThenUnknownByNameAsc} from '../../helpers/comparators';
import {translate} from '../../services/translationService';
import {Address, City} from '../../state/domain-models/location/locationModels';
import {
  FetchByPage,
  PagedResponse,
} from '../../state/domain-models/selections/selectionsApiActions';
import {SelectionListItem} from '../../state/user-selection/userSelectionModels';
import {Children, IdNamed, uuid} from '../../types/Types';
import {IconDropDown} from '../icons/IconDropDown';
import {Column} from '../layouts/column/Column';
import {Row, RowMiddle} from '../layouts/row/Row';
import {Normal} from '../texts/Texts';
import {Checkbox} from './Checkbox';
import {
  replaceWhereId,
  searchOverviewText,
  throttledSearch,
  ThrottledSearch,
  unknownItems,
} from './dropdownHelper';
import './DropdownSelector.scss';
import {SearchBox} from './SearchBox';
import origin = __MaterialUI.propTypes.origin;

interface OptionalProps {
  renderLabel?: (index: number, items: SelectionListItem[]) => Children;
  rowHeight?: number;
  visibleItems?: number;
}

interface SimpleProps {
  fetchItems: FetchByPage;
  select: (props: SelectionListItem) => void;
  selectedItems: SelectionListItem[];
  selectionText: string;
}

interface Props extends SimpleProps {
  fetchItemsByQuery?: ThrottledSearch<PagedResponse>;
  unknownItem?: SelectionListItem;
}

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

export type DropdownSelectorProps = Props & Required<OptionalProps>;

class PaginatedDropdownSelector extends React.Component<DropdownSelectorProps, State> {

  constructor(props: DropdownSelectorProps) {
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

  componentWillReceiveProps({selectedItems}: DropdownSelectorProps) {
    if (!this.state.isOpen && this.props.selectedItems.length !== selectedItems.length) {
      const selectedItemIds = selectedItems.map(getId);
      this.setState((prevState: State) => {
        const items = prevState.items.map((item: SelectionListItem) => ({
          ...item,
          selected: selectedItemIds.includes(item.id),
        }));
        return ({items, cache: {items, totalElements: prevState.cache.totalElements}});
      });
    }
  }

  render() {
    const {anchorElement, cache, isOpen, isSearching, items, totalElements, query} = this.state;
    const {fetchItemsByQuery, selectionText, selectedItems} = this.props;

    const selectedOverview: string = searchOverviewText(selectedItems, cache.totalElements);

    const numSelectedItems: number = selectedItems.length;
    const numItems: number = items.length;
    const offset: number = isSearching ? 0 : 1;

    const rowCount = (numItems - numSelectedItems + offset) < totalElements
      ? numItems + offset
      : totalElements;

    const renderSearchBox =
      fetchItemsByQuery && <SearchBox onChange={this.onUpdateSearch} clear={!isOpen}/>;

    return (
      <Row className="DropdownSelector">
        <div
          onClick={this.openMenu}
          className={classNames('DropdownSelector-Text clickable', {isOpen})}
        >
          <RowMiddle>
            <Normal className="first-uppercase">{selectionText}{selectedOverview}</Normal>
            <IconDropDown/>
          </RowMiddle>
        </div>

        <Popover
          style={dropDownStyle.popoverStyle}
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
    this.setState(
      {
        isOpen: true,
        anchorElement: event.currentTarget,
        items: [...this.state.cache.items].sort(selectedFirstThenUnknownByNameAsc),
      },
    );
  }

  closeMenu = (): void => this.setState({
    isOpen: false,
    isSearching: false,
    totalElements: this.state.cache.totalElements,
  })

  onUpdateSearch = (searchText: string) => {
    const {fetchItemsByQuery} = this.props;
    fetchItemsByQuery!(
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
          this.setState((prevState: State) => ({
            items: [...prevState.cache.items],
            isSearching: false,
            page: 0,
            totalElements: prevState.cache.totalElements,
            query: undefined,
          }));
        }
      },
    );
  }

  onSelect = (selectedItem: SelectionListItem, id: uuid) => {
    const newItem = {...selectedItem, selected: !selectedItem.selected};
    this.props.select(newItem);
    this.setState((prevState) => ({
      cache: {
        items: replaceWhereId(
          prevState.cache.items,
          newItem,
          id,
        ),
        totalElements: prevState.cache.totalElements,
      },
    }));
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
        className={classNames('first-uppercase', {Bold: selected})}
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
        style={dropDownStyle.listStyle}
        width={240}
      />
    );
  }

  isRowLoaded = ({index}: Index): boolean => !!this.state.items[index];

  loadMoreRows = async (): Promise<SelectionListItem[] | {}> => {
    const {selectedItems, fetchItems} = this.props;
    const {items, totalElements} = await fetchItems(this.state.page);

    return new Promise((resolve) => {
      const selectedIds = selectedItems.map(getId);
      const unselected = items
        .map((item: SelectionListItem) => ({...item, selected: false}))
        .filter((item: SelectionListItem) => !selectedIds.includes(item.id));

      this.setState((prevState: State) => {
        const items = [...prevState.items, ...unselected];
        return ({
          items,
          cache: {items, totalElements},
          totalElements,
          page: this.state.page + 1,
        });
      });
      return resolve(items);
    });
  }

}

const translatedNameOf = ({name}: IdNamed): string =>
  name === 'unknown' ? translate('unknown') : name;

const renderLabels = (name: string, parentName: string) => {
  return (
    <Column>
      <Normal className="first-uppercase">{name}</Normal>
      <div className="first-uppercase" style={dropDownStyle.parentStyle}>{parentName}</div>
    </Column>
  );
};

const renderLabelAtIndex = (index: number, filteredList: SelectionListItem[]) => {
  const {name} = filteredList[index];
  return <Normal>{name}</Normal>;
};

export const renderCityLabel = (index: number, filteredList: SelectionListItem[]) => {
  const city = filteredList[index] as City;
  return renderLabels(translatedNameOf(city), translatedNameOf(city.country));
};

export const renderAddressLabel = (index: number, filteredList: SelectionListItem[]) => {
  const address = filteredList[index] as Address;
  return renderLabels(translatedNameOf(address), translatedNameOf(address.city));
};

const DropdownSelector = (props: Props & OptionalProps) => (
  <PaginatedDropdownSelector
    renderLabel={renderLabelAtIndex}
    rowHeight={40}
    visibleItems={10}
    {...props}
  />
);

export const SearchDropdownSelector = (props: Props & OptionalProps) => (
  <DropdownSelector
    fetchItemsByQuery={throttledSearch(props.fetchItems)}
    {...props}
  />
);

export const SimpleDropdownSelector = (props: SimpleProps & OptionalProps) => (
  <DropdownSelector {...props}/>
);
