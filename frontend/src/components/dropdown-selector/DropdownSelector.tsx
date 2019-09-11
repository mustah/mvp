import Popover from 'material-ui/Popover/Popover';
import PopoverAnimationVertical from 'material-ui/Popover/PopoverAnimationVertical';
import * as React from 'react';
import {Index, List, ListRowProps} from 'react-virtualized';
import {style as typestyle} from 'typestyle';
import {dropdownStyle} from '../../app/themes';
import {getId} from '../../helpers/collections';
import {selectedFirstThenUnknownByNameAsc} from '../../helpers/comparators';
import {orUnknown} from '../../helpers/translations';
import {firstUpper} from '../../services/translationService';
import {Address, City} from '../../state/domain-models/location/locationModels';
import {FetchByPage, PagedResponse} from '../../state/domain-models/selections/selectionsModels';
import {SelectionListItem} from '../../state/user-selection/userSelectionModels';
import {Children, Fetching, IdNamed, uuid} from '../../types/Types';
import {ThemeContext, withCssStyles} from '../hoc/withThemeProvider';
import {Column} from '../layouts/column/Column';
import {RowLeft} from '../layouts/row/Row';
import {SearchBox, SearchBoxProps} from '../search-box/SearchBox';
import {LabelWithSubtitle} from '../texts/Labels';
import {FirstUpper} from '../texts/Texts';
import {Checkbox} from './Checkbox';
import {replaceWhereId, searchOverviewText, throttledSearch, ThrottledSearch, unknownItems} from './dropdownHelper';
import {DropdownList} from './DropdownList';
import {DropDownSelectorButton} from './DropdownSelectorButton';
import origin = __MaterialUI.propTypes.origin;

type OptionalProps = Partial<{
  renderLabel: (index: number, items: SelectionListItem[]) => Children;
  rowHeight: number;
  overviewText: (list: SelectionListItem[], totalElements: number) => string;
  visibleItems: number;
}>;

interface Props {
  innerDivStyle?: React.CSSProperties;
  innerDivClassName?: string;
  fetchItems: FetchByPage;
  isReadOnlyList?: boolean;
  shouldFetchItemsOnMount?: boolean;
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

interface State extends Fetching, PagedResponse {
  anchorElement?: React.ReactInstance;
  cache: Cache;
  isOpen: boolean;
  isSearching: boolean;
  page: number;
}

const anchorOrigin: origin = {horizontal: 'left', vertical: 'bottom'};
const targetOrigin: origin = {horizontal: 'left', vertical: 'top'};
const listWidth = 240;

export type SearchableProps = QueryProps & Props & OptionalProps;
export type DropdownComponentProps = QueryProps & Props & Required<OptionalProps> & ThemeContext;

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
      isFetching: false,
      items,
      page: 0,
      totalElements: 0,
    };
  }

  async componentDidMount() {
    if (this.props.shouldFetchItemsOnMount) {
      await this.loadMoreRows();
    } else {
      this.setState({isFetching: true});
    }
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
    const {anchorElement, cache, isFetching, isOpen, isSearching, items, totalElements, query} = this.state;
    const {
      cssStyles,
      innerDivClassName,
      innerDivStyle,
      fetchItemsByQuery,
      overviewText,
      selectionText,
      selectedItems
    } = this.props;

    const selectedOverview: string = overviewText(selectedItems, cache.totalElements);

    const numSelectedItems: number = selectedItems.length;
    const numItems: number = items.length;
    const offset: number = isSearching || !fetchItemsByQuery ? 0 : 1;

    const rowCount = (numItems - numSelectedItems + offset) < totalElements
      ? numItems + offset
      : totalElements;

    const popoverContentClassName = typestyle({
      margin: '8px 8px',
      width: listWidth,
      $nest: {
        '.DropdownSelector-content': {
          maxHeight: 420,
          overflowY: 'scroll',
        },
      }
    });

    const searchBoxProps: SearchBoxProps = {
      cssStyles,
      clear: !isOpen,
      onChange: this.onUpdateSearch,
      onClear: this.onClear
    };

    const searchBox = fetchItemsByQuery && <SearchBox {...searchBoxProps}/>;

    return (
      <>
        <DropDownSelectorButton
          className={innerDivClassName}
          isOpen={isOpen}
          onClick={this.openMenu}
          style={innerDivStyle}
          text={selectionText + selectedOverview}
        />

        <Popover
          style={dropdownStyle.popoverStyle}
          open={isOpen}
          anchorEl={anchorElement}
          anchorOrigin={anchorOrigin}
          targetOrigin={targetOrigin}
          onRequestClose={this.closeMenu}
          animation={PopoverAnimationVertical}
        >
          <Column className={popoverContentClassName}>
            {searchBox}
            <DropdownList
              isFetching={isFetching}
              isRowLoaded={this.isRowLoaded}
              key={query}
              loadMoreRows={this.loadMoreRows}
              rowCount={rowCount}
            >
              {this.renderList}
            </DropdownList>
          </Column>
        </Popover>
      </>
    );
  }

  openMenu = (event: any): void => {
    event.preventDefault();
    const {selectedItems} = this.props;
    this.setState({
      isOpen: true,
      anchorElement: event.currentTarget,
      items: listItems(selectedItems, this.state.cache.items).sort(selectedFirstThenUnknownByNameAsc),
    });
    this.shouldFetchItems();
  }

  async shouldFetchItems(): Promise<void> {
    if (this.state.isFetching) {
      await this.loadMoreRows();
    }
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
          const {selectedItems} = this.props;
          this.setState({
            items: withNewItems(selectedItems, items),
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
      items: withNewItems(selectedItems, items).sort(selectedFirstThenUnknownByNameAsc),
      isSearching: false,
      page: 0,
      totalElements,
      query: undefined,
    }));
  }

  onSelect = (selectedItem: SelectionListItem, id: uuid) => {
    const newItem = {...selectedItem, selected: !selectedItem.selected};
    this.props.select(newItem);
    this.setState(({items}) => ({items: replaceWhereId(items, newItem, id)}));
  }

  rowRenderer = ({index, style}: ListRowProps) => {
    const {items} = this.state;
    const {cssStyles: {primary}, isReadOnlyList} = this.props;
    const selectedItem = items[index];
    const {id, selected} = selectedItem;
    const onClick = () => this.onSelect(selectedItem, id);
    const label = this.props.renderLabel(index, items)!;
    const listItemProps = {
      className: typestyle({$nest: {'&.Checkbox:hover': {backgroundColor: primary.bgHover}}}),
      id,
      key: `${index}-${id}`,
      label,
      labelClassName: 'first-uppercae',
      style,
    };
    if (isReadOnlyList) {
      return <RowLeft {...listItemProps}>{label}</RowLeft>;
    } else {
      return <Checkbox onClick={onClick} checked={selected} {...listItemProps}/>;
    }
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
        width={listWidth}
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
          isFetching: false,
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

const DropdownSelector = withCssStyles((props: SearchableProps & ThemeContext) => (
  <DropdownComponent
    isReadOnlyList={false}
    overviewText={searchOverviewText}
    renderLabel={renderLabelAtIndex}
    rowHeight={40}
    shouldFetchItemsOnMount={true}
    visibleItems={10}
    {...props}
  />
));

export const SearchableDropdownSelector = (props: SearchableProps) => (
  <DropdownSelector
    fetchItemsByQuery={throttledSearch(props.fetchItems)}
    {...props}
  />
);

export const ListingDropdownSelector = (props: Props & OptionalProps) => (
  <DropdownSelector {...props}/>
);
