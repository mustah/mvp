import * as classNames from 'classnames';
import 'DropDownSelector.scss';
import Menu from 'material-ui/Menu';
import Popover from 'material-ui/Popover/Popover';
import * as React from 'react';
import {Clickable} from '../../../types/Types';
import {Icon} from '../../common/components/icons/Icons';
import {Column} from '../../common/components/layouts/column/Column';
import {Row, RowMiddle} from '../../common/components/layouts/row/Row';
import {Normal} from '../../common/components/texts/Texts';
import {CheckboxList, CheckboxProps} from './CheckboxList';

interface OwnProps extends Clickable {
  name: string;
}

interface Props {
  selectionText: string;
  list: CheckboxProps[];
}

interface State extends Props {
  isOpen: boolean;
  anchorElement?: React.ReactInstance;
}

export class DropDownSelector extends React.Component<Props & OwnProps, State> {

  constructor(props) {
    super(props);
    this.state = {
      isOpen: false,
      selectionText: props.selectionText,
      list: props.list,
    };
  }

  render() {
    const {anchorElement, isOpen, list, selectionText} = this.state;
    return (
      <Row className="DropDownSelector">
        <div onClick={this.openMenu} className={classNames('DropDownSelector-Text clickable', {isOpen})}>
          <RowMiddle>
            <Normal>{selectionText}</Normal>
            <Icon name="chevron-down" size="small"/>
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
            <Column>
              <CheckboxList onClick={this.onClick} list={list}/>
            </Column>
          </Menu>
        </Popover>
      </Row>
    );
  }

  onClick = (args): void => {
    const {onClick, name} = this.props;
    onClick({...args, name});
  }

  openMenu = (event: React.SyntheticEvent<any>): void => {
    event.preventDefault();
    this.setState({isOpen: true, anchorElement: event.currentTarget});
  }

  closeMenu = (): void => {
    this.setState({isOpen: false});
  }
}
