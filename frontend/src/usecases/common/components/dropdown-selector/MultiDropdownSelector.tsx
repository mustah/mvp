import * as classNames from 'classnames';
import Popover from 'material-ui/Popover/Popover';
import PopoverAnimationVertical from 'material-ui/Popover/PopoverAnimationVertical';
import * as React from 'react';
import {List, ListRowProps} from 'react-virtualized';
import {translate} from '../../../../services/translationService';
import {SelectionListItem} from '../../../../state/search/selection/selectionModels';
import {IdNamed} from '../../../../types/Types';
import {dropdownRowStyle, dropDownStyle} from '../../../app/themes';
import {IconDropDown} from '../icons/IconDropDown';
import {Column} from '../layouts/column/Column';
import {Row, RowMiddle} from '../layouts/row/Row';
import {Normal} from '../texts/Texts';
import {Checkbox} from './Checkbox';
import './DropdownSelector.scss';
import {SearchBox} from './SearchBox';

interface Props {
  selectionText: string;
  list: SelectionListItem[];
  select: (props: IdNamed) => void;
}

interface State {
  isOpen: boolean;
  searchText: string;
  anchorElement?: React.ReactInstance;
  filteredList: SelectionListItem[];
}

const filterBy = (list: SelectionListItem[], exp: string) => {
  const re = new RegExp(exp, 'i');
  return list.filter((value: IdNamed) => value.name.match(re));
};

const selectedOptions = (list: SelectionListItem[]) => list.filter((item: SelectionListItem) => item.selected).length;

const replaceArrayItem = (array: any[], newItem: any, index: number): any[] =>
  ([...array.slice(0, index), newItem, ...array.slice(index + 1)]);

export class MultiDropdownSelector extends React.PureComponent<Props, State> {

  constructor(props) {
    super(props);
    this.state = {
      isOpen: false,
      searchText: '',
      filteredList: [],
    };
  }

  render() {
    const {anchorElement, isOpen, searchText, filteredList} = this.state;
    const {selectionText, list} = this.props;

    const rowHeight = dropdownRowStyle.rowHeightMulti;
    const visibleItems = dropdownRowStyle.visibleItemsMulti;
    const entries = filteredList.length;

    const selected = selectedOptions(list);
    const selectedOverview = selected && selected + ' / ' + list.length || translate('all');

    return (
      <Row className="DropdownSelector">
        <div onClick={this.openMenu} className={classNames('DropdownSelector-Text clickable', {isOpen})}>
          <RowMiddle>
            <Normal className="capitalize">{selectionText}{selectedOverview}</Normal>
            <IconDropDown/>
          </RowMiddle>
        </div>

        <Popover
          style={dropDownStyle.popoverStyle}
          open={isOpen}
          anchorEl={anchorElement}
          anchorOrigin={{horizontal: 'left', vertical: 'bottom'}}
          targetOrigin={{horizontal: 'left', vertical: 'top'}}
          onRequestClose={this.closeMenu}
          animation={PopoverAnimationVertical}
        >
          <Column className="DropdownSelector-menu">
            <SearchBox value={searchText} onUpdateSearch={this.whenSearchUpdate}/>
            <Row>
              <List
                height={entries > visibleItems ? visibleItems * rowHeight : entries * rowHeight}
                overscanRowCount={10}
                rowCount={entries}
                rowHeight={rowHeight}
                rowRenderer={this.rowRenderer}
                width={240}
                style={dropDownStyle.listStyle}
              />
            </Row>
          </Column>
        </Popover>
      </Row>
    );
  }

  openMenu = (event: React.SyntheticEvent<any>): void => {
    event.preventDefault();
    this.setState({
      isOpen: true,
      anchorElement: event.currentTarget,
      filteredList: [...this.props.list],
    });
  }

  closeMenu = (): void => {
    this.setState({isOpen: false, searchText: ''});
  }

  whenSearchUpdate = (event) => {
    event.preventDefault();
    this.setState({
      searchText: event.target.value,
      filteredList: filterBy(this.props.list, event.target.value),
    });
  }

  onSelect = ({id, name, index}: IdNamed & {index: number}) => {
    const {filteredList} = this.state;
    const selectedItem = filteredList[index];

    this.props.select({id, name});
    this.setState({
      filteredList: replaceArrayItem(filteredList, {...selectedItem, selected: !selectedItem.selected}, index),
    });
  }

  rowRenderer = ({index, style}: ListRowProps) => {
    const {filteredList} = this.state;
    const {id, name, selected} = filteredList[index];
    const onClick = () => this.onSelect({id, name, index});
    return (
      <Checkbox
        id={id}
        name={name}
        onClick={onClick}
        key={id}
        style={style}
        checked={selected}
      />
    );
  }
}
