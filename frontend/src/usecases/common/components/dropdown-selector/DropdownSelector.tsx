import * as classNames from 'classnames';
import Menu from 'material-ui/Menu';
import Popover from 'material-ui/Popover/Popover';
import PopoverAnimationVertical from 'material-ui/Popover/PopoverAnimationVertical';
import * as React from 'react';
import {List, ListRowProps} from 'react-virtualized';
import {translate} from '../../../../services/translationService';
import {SelectionListItem} from '../../../../state/search/selection/selectionModels';
import {Clickable, IdNamed} from '../../../../types/Types';
import {dropDownStyle} from '../../../app/themes';
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

export class DropdownSelector extends React.PureComponent<Props & Clickable, State> {

  constructor(props) {
    super(props);
    this.state = {
      isOpen: false,
      searchText: '',
      filteredList: [],
    };
  }

  render() {
    const {anchorElement, isOpen, searchText} = this.state;
    const {selectionText} = this.props;

    const rowHeight = 20; // TODO: Should prorably be move somewhere else.
    const visibleItems = 15;
    const entries = this.state.filteredList.length;

    const selected = selectedOptions(this.props.list);
    const selectedOverview = selected && selected + ' / ' + this.props.list.length || translate('all');

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
          <Menu>
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
          </Menu>
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

  onClick = (props) => {
    const {filteredList} = this.state;
    const item = filteredList[props.index];
    this.props.onClick(props);
    this.setState({
      filteredList: [
        ...filteredList.slice(0, props.index),
        {...item, selected: !item.selected},
        ...filteredList.slice(props.index + 1),
      ],
    });
  }

  rowRenderer = ({index, style}: ListRowProps) => {
    const {filteredList} = this.state;
    const item = filteredList[index];
    const onClick = (props: IdNamed) => this.onClick({...props, index});
    return (
      <Checkbox
        id={item.id}
        name={item.name}
        onClick={onClick}
        key={item.id}
        style={style}
        checked={item.selected}
      />
    );
  }
}
