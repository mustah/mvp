import * as classNames from 'classnames';
import './DropdownSelector.scss';
import Menu from 'material-ui/Menu';
import Popover from 'material-ui/Popover/Popover';
import * as React from 'react';
import {Clickable, IdNamed} from '../../../../types/Types';
import {IconDropDown} from '../icons/IconDropDown';
import {Column} from '../layouts/column/Column';
import {Row, RowMiddle} from '../layouts/row/Row';
import {Normal} from '../texts/Texts';
import {CheckboxList} from './CheckboxList';
import {SearchBox} from './SearchBox';

interface Props {
  selectionText: string;
  selectedList: IdNamed[];
  list: IdNamed[];
}

interface State {
  isOpen: boolean;
  anchorElement?: React.ReactInstance;
}

export class DropdownSelector extends React.Component<Props & Clickable, State> {

  constructor(props) {
    super(props);
    this.state = {
      isOpen: false,
    };
  }

  shouldComponentUpdate(nextProps, nextState) {
      return this.state.isOpen !== nextState.isOpen;
  }

  render() {
    const {anchorElement, isOpen} = this.state;
    const {selectionText, list, selectedList, onClick} = this.props;
    return (
      <Row className="DropdownSelector">
        <div onClick={this.openMenu} className={classNames('DropdownSelector-Text clickable', {isOpen})}>
          <RowMiddle>
            <Normal>{selectionText}</Normal>
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
          animated={false}
        >
          <Menu>
            <Column className="DropdownSelector-menu">
              <SearchBox/>
              <CheckboxList onClick={onClick} list={selectedList} allChecked={true}/>
              {selectedList && selectedList.length > 0 && <Row className="separation-border"/>}
              <CheckboxList onClick={onClick} list={list}/>
            </Column>
          </Menu>
        </Popover>
      </Row>
    );
  }

  openMenu = (event: React.SyntheticEvent<any>): void => {
    event.preventDefault();
    this.setState({isOpen: true, anchorElement: event.currentTarget});
  }

  closeMenu = (): void => {
    this.setState({isOpen: false});
  }
}
