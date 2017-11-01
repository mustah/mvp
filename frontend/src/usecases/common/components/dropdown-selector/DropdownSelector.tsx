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
  selectedList: IdNamed[];
  list: IdNamed[];
}

const filterBy = (list: IdNamed[], exp: string) => {
  const re = new RegExp(exp, 'i');
  return list.filter((value: IdNamed) => value.name.match(re));
};

export class DropdownSelector extends React.Component<Props & Clickable, State> {

  constructor(props) {
    super(props);
    this.state = {
      isOpen: false,
      searchText: '',
      selectedList: [],
      list: [],
    };
  }

  render() {
    const {anchorElement, isOpen, searchText, list, selectedList} = this.state;
    const {selectionText, onClick} = this.props;

    const selectedOptions = this.props.selectedList.length;
    const totalNumberOfOptions = selectedOptions + this.props.list.length;

    const selectedOverview = selectedOptions && selectedOptions + ' / ' + totalNumberOfOptions || translate('all');

    const filteredList = filterBy(list, searchText);
    const filteredSelectedList = filterBy(selectedList, searchText);
    return (
      <Row className="DropdownSelector">
        <div onClick={this.openMenu} className={classNames('DropdownSelector-Text clickable', {isOpen})}>
          <RowMiddle>
            <Normal className="capitalize">{selectionText}{selectedOverview}</Normal>
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
              <SearchBox value={searchText} onUpdateSearch={this.whenSearchUpdate}/>
              <CheckboxList onClick={onClick} list={filteredSelectedList} allChecked={true}/>
              {selectedList && selectedList.length > 0 && <Row className="separation-border"/>}
              <CheckboxList onClick={onClick} list={filteredList}/>
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
      selectedList: [...this.props.selectedList],
    });
  }

  closeMenu = (): void => {
    this.setState({isOpen: false, searchText: ''});
  }

  whenSearchUpdate = (event) => { // TODO: add typing to event?
    event.preventDefault();
    this.setState({searchText: event.target.value});
  }

}
