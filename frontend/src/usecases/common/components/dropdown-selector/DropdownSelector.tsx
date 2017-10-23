import * as classNames from 'classnames';
import Menu from 'material-ui/Menu';
import Popover from 'material-ui/Popover/Popover';
import * as React from 'react';
import {Clickable, IdNamed} from '../../../../types/Types';
import {IconDropDown} from '../icons/IconDropDown';
import {Column} from '../layouts/column/Column';
import {Row, RowMiddle} from '../layouts/row/Row';
import {Normal} from '../texts/Texts';
import {CheckboxList} from './CheckboxList';
import './DropdownSelector.scss';
import {SearchBox} from './SearchBox';
import {translate} from '../../../../services/translationService';

interface Props {
  selectionText: string;
  selectedList: IdNamed[];
  list: IdNamed[];
}

interface State {
  isOpen: boolean;
  searchText: string;
  anchorElement?: React.ReactInstance;
  localSelectedList: IdNamed[];
  localList: IdNamed[];
}

export class DropdownSelector extends React.Component<Props & Clickable, State> {

  constructor(props) {
    super(props);
    this.state = {
      isOpen: false,
      searchText: '',
      localSelectedList: [],
      localList: [],
    };
  }

  render() {
    const {anchorElement, isOpen} = this.state;
    const {selectionText, list, selectedList, onClick} = this.props;

    const selectedOptions = selectedList.length;
    const totalNumberOfOptions = selectedOptions + list.length;

    const selectedOverview = selectedOptions && selectedOptions + ' / ' + totalNumberOfOptions || translate('all');

    return (
      <Row className="DropdownSelector">
        <div onClick={this.openMenu} className={classNames('DropdownSelector-Text clickable', {isOpen})}>
          <RowMiddle>
            <Normal>{selectionText}{selectedOverview}</Normal>
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
              <SearchBox value={this.state.searchText} onUpdateSearch={this.handleSearchUpdate}/>
              <CheckboxList onClick={onClick} list={this.state.localSelectedList} allChecked={true}/>
              {selectedList && selectedList.length > 0 && <Row className="separation-border"/>}
              <CheckboxList onClick={onClick} list={this.state.localList}/>
            </Column>
          </Menu>
        </Popover>
      </Row>
    );
  }

  openMenu = (event: React.SyntheticEvent<any>): void => {
    const {list, selectedList} = this.props;
    event.preventDefault();
    this.setState({isOpen: true, anchorElement: event.currentTarget, localList: list, localSelectedList: selectedList});
  }

  closeMenu = (): void => {
    this.setState({isOpen: false});
  }

  handleSearchUpdate = (event) => { // TODO: add typing to event?
      event.preventDefault();
      this.setState({searchText: event.target.value });
  }
}
