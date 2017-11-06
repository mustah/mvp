import * as classNames from 'classnames';
import Menu from 'material-ui/Menu';
import Popover from 'material-ui/Popover/Popover';
import PopoverAnimationVertical from 'material-ui/Popover/PopoverAnimationVertical';
import * as React from 'react';
import {List, ListRowProps} from 'react-virtualized';
import {SelectionListItem} from '../../../../state/search/selection/selectionSelectors';
import {Clickable, IdNamed} from '../../../../types/Types';
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
  list: SelectionListItem[];
  filteredList: SelectionListItem[];
  scrollToIndex: number;
}

const filterBy = (list: SelectionListItem[], exp: string) => {
  const re = new RegExp(exp, 'i');
  return list.filter((value: IdNamed) => value.name.match(re));
};

export class DropdownSelector extends React.PureComponent<Props & Clickable, State> {

  constructor(props) {
    super(props);
    this.state = {
      isOpen: false,
      searchText: '',
      list: [],
      filteredList: [],
      scrollToIndex: 0,
    };
  }

  render() {
    const {anchorElement, isOpen, searchText, scrollToIndex} = this.state;
    const {selectionText} = this.props;

    return (
      <Row className="DropdownSelector">
        <div onClick={this.openMenu} className={classNames('DropdownSelector-Text clickable', {isOpen})}>
          <RowMiddle>
            <Normal className="capitalize">{selectionText}</Normal>
            <IconDropDown/>
          </RowMiddle>
        </div>

        <Popover
          style={{marginTop: '6px', marginLeft: '2px'}}
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
              <List
                height={this.state.list.length > 10 ? 400 : this.state.list.length * 20}
                overscanRowCount={10}
                rowCount={this.state.filteredList.length}
                rowHeight={20}
                rowRenderer={this.rowRenderer}
                width={200}
                scrollToIndex={scrollToIndex}
              />
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
      list: [...this.props.list],
      filteredList: [...this.props.list],
    });
  }

  closeMenu = (): void => {
    this.setState({isOpen: false, searchText: '', scrollToIndex: 0});
  }

  whenSearchUpdate = (event) => {
    event.preventDefault();
    this.setState({
      searchText: event.target.value,
      scrollToIndex: 0,
      filteredList: filterBy(this.state.list, event.target.value),
    });
  }

  rowRenderer = ({index, key, style}: ListRowProps) => {
    const {filteredList} = this.state;
    const {onClick} = this.props;
    return (
      <Checkbox
        id={filteredList[index].id}
        name={filteredList[index].name}
        onClick={onClick}
        key={key}
        style={style}
        checked={filteredList[index].checked}
      />
    );
  }
}
