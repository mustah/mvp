import * as classNames from 'classnames';
import Popover from 'material-ui/Popover/Popover';
import PopoverAnimationVertical from 'material-ui/Popover/PopoverAnimationVertical';
import * as React from 'react';
import {Index, InfiniteLoader, List, ListRowProps} from 'react-virtualized';
import {dropDownStyle} from '../../app/themes';
import {selectedFirstThenUnknownByNameAsc} from '../../helpers/comparators';
import {firstUpperTranslated, translate} from '../../services/translationService';
import {Address, City} from '../../state/domain-models/location/locationModels';
import {
  FetchByPage,
  PagedResponse,
} from '../../state/domain-models/selections/selectionsApiActions';
import {SelectionListItem} from '../../state/user-selection/userSelectionModels';
import {Children, IdNamed} from '../../types/Types';
import {IconDropDown} from '../icons/IconDropDown';
import {Column} from '../layouts/column/Column';
import {Row, RowMiddle} from '../layouts/row/Row';
import {Normal} from '../texts/Texts';
import {Checkbox} from './Checkbox';
import './DropdownSelector.scss';
import {SearchBox} from './SearchBox';
import origin = __MaterialUI.propTypes.origin;

interface OptionalProps {
  renderLabel?: (index: number, items: SelectionListItem[]) => Children;
  rowHeight?: number;
  visibleItems?: number;
}

interface Props {
  fetchItems: FetchByPage;
  selectionText: string;
  selectedItems: SelectionListItem[];
  select: (props: SelectionListItem) => void;
}

interface State extends PagedResponse {
  isOpen: boolean;
  searchText: string;
  anchorElement?: React.ReactInstance;
  page: number;
}

const filterBy = (list: SelectionListItem[], exp: string) => {
  const regExp = new RegExp(exp, 'i');
  return list.filter(({name}: IdNamed) => regExp.test(name));
};

const selectedOptions = (list: SelectionListItem[]): number =>
  list.filter((item: SelectionListItem) => item.selected).length;

const replaceAtIndex = (
  array: SelectionListItem[],
  newItem: SelectionListItem,
  index: number,
): SelectionListItem[] =>
  ([...array.slice(0, index), newItem, ...array.slice(index + 1)]);

const searchOverviewText = (list: SelectionListItem[]): string => {
  const numSelected: number = selectedOptions(list);
  return numSelected && numSelected + ' / ' + list.length || firstUpperTranslated('all');
};

const anchorOrigin: origin = {horizontal: 'left', vertical: 'bottom'};
const targetOrigin: origin = {horizontal: 'left', vertical: 'top'};

type OwnProps = Props & Required<OptionalProps>;

class PaginatedDropdownSelector extends React.Component<OwnProps, State> {

  constructor(props: OwnProps) {
    super(props);
    this.state = {
      isOpen: false,
      searchText: '',
      items: [...props.selectedItems],
      totalElements: 0,
      page: 0,
    };
  }

  async componentDidMount() {
    await this.loadMoreRows();
  }

  render() {
    const {anchorElement, isOpen, searchText, items, totalElements} = this.state;
    const {selectionText, selectedItems} = this.props;

    const selectedOverview: string = searchOverviewText(selectedItems);

    const numSelectedItems: number = selectedItems.length;
    const numItems = items.length;

    const rowCount = (numItems - numSelectedItems + 1) < totalElements
      ? numItems + 1
      : totalElements;

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
            <SearchBox value={searchText} onUpdateSearch={this.whenSearchUpdate}/>
            <Row>
              <InfiniteLoader
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
        items: [...this.state.items].sort(selectedFirstThenUnknownByNameAsc),
      },
    );
  }

  closeMenu = (): void => this.setState({isOpen: false, searchText: ''});

  whenSearchUpdate = (event: any) => {
    const {selectedItems} = this.props;
    event.preventDefault();
    this.setState({
      searchText: event.target.value,
      items: filterBy(selectedItems, event.target.value),
    });
  }

  onSelect = (selectedItem: SelectionListItem, index: number) => {
    const newItem = {...selectedItem, selected: !selectedItem.selected};
    this.props.select(newItem);
    this.setState((prevState) => ({items: replaceAtIndex(prevState.items, newItem, index)}));
  }

  rowRenderer = ({index, style}: ListRowProps) => {
    const {items} = this.state;
    const selectedItem = items[index];
    const {id, selected} = selectedItem;
    const onClick = () => this.onSelect(selectedItem, index);
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
      const selectedIds = selectedItems.map((item: SelectionListItem) => item.id);
      const unselected = items
        .map((item: SelectionListItem) => ({...item, selected: false}))
        .filter((item: SelectionListItem) => !selectedIds.includes(item.id));

      this.setState((prevState: State) => ({
        items: [...prevState.items, ...unselected],
        totalElements,
        page: prevState.page + 1,
      }));
      return resolve(items);
    });
  }

}

const translatedNameOf = ({name}: IdNamed): string =>
  name === 'unknown' ? translate('unknown') : name;

const renderLabels = (name: string, parentName: string) => {
  return ([
    <Normal key={1}>{name}</Normal>,
    <div className="first-uppercase" key={2} style={dropDownStyle.parentStyle}>{parentName}</div>,
  ]);
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

export const DropdownSelector = (props: Props & OptionalProps) => (
  <PaginatedDropdownSelector
    renderLabel={renderLabelAtIndex}
    rowHeight={40}
    visibleItems={10}
    {...props}
  />
);
