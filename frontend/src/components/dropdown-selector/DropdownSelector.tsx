import * as classNames from 'classnames';
import Popover from 'material-ui/Popover/Popover';
import PopoverAnimationVertical from 'material-ui/Popover/PopoverAnimationVertical';
import * as React from 'react';
import {List, ListRowProps} from 'react-virtualized';
import {dropDownStyle} from '../../app/themes';
import {firstUpperTranslated} from '../../services/translationService';
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

export interface DropdownProps {
  selectionText: string;
  list: SelectionListItem[];
  select: (props: IdNamed) => void;
}

interface GenericDropdownProps extends DropdownProps {
  renderLabel: (index: number, filteredList: SelectionListItem[]) => Children;
  rowHeight: number;
  visibleItems: number;
}

interface State {
  isOpen: boolean;
  searchText: string;
  anchorElement?: React.ReactInstance;
  filteredList: SelectionListItem[];
}

const filterBy = (list: SelectionListItem[], exp: string) => {
  const regExp = new RegExp(exp, 'i');
  return list.filter(({name}: IdNamed) => regExp.test(name));
};

const selectedOptions = (list: SelectionListItem[]) => list.filter((item: SelectionListItem) => item.selected).length;

const replaceAtIndex = (array: SelectionListItem[], newItem: SelectionListItem, index: number): SelectionListItem[] =>
  ([...array.slice(0, index), newItem, ...array.slice(index + 1)]);

const anchorOrigin: origin = {horizontal: 'left', vertical: 'bottom'};
const targetOrigin: origin = {horizontal: 'left', vertical: 'top'};

export class DropdownSelector extends React.PureComponent<GenericDropdownProps, State> {

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

    const rowHeight = this.props.rowHeight;
    const visibleItems = this.props.visibleItems;
    const numEntries = filteredList.length;

    const selected = selectedOptions(list);
    const selectedOverview = selected && selected + ' / ' + list.length || firstUpperTranslated('all');

    return (
      <Row className="DropdownSelector">
        <div onClick={this.openMenu} className={classNames('DropdownSelector-Text clickable', {isOpen})}>
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
              <List
                height={numEntries > visibleItems ? visibleItems * rowHeight : numEntries * rowHeight}
                overscanRowCount={10}
                rowCount={numEntries}
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

  openMenu = (event: any): void => {
    event.preventDefault();
    this.setState({
      isOpen: true,
      anchorElement: event.currentTarget,
      filteredList: [...this.props.list],
    });
  }

  closeMenu = (): void => this.setState({isOpen: false, searchText: ''});

  whenSearchUpdate = (event: any) => {
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
      filteredList: replaceAtIndex(filteredList, {...selectedItem, selected: !selectedItem.selected}, index),
    });
  }

  rowRenderer = ({index, style}: ListRowProps) => {
    const {filteredList} = this.state;
    const {id, name, selected} = filteredList[index];
    const onClick = () => this.onSelect({id, name, index});
    const label = this.props.renderLabel(index, filteredList);

    return (
      <Checkbox
        id={id}
        label={label}
        onClick={onClick}
        key={id}
        style={style}
        className="first-uppercase"
        checked={selected}
      />
    );
  }
}
