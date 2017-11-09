import Menu from 'material-ui/Menu';
import MenuItem from 'material-ui/MenuItem';
import Popover from 'material-ui/Popover/Popover';
import * as React from 'react';
import {IconMore} from '../../icons/IconMore';
import {Column} from '../../layouts/column/Column';
import {Row} from '../../layouts/row/Row';
import {TabUnderline} from './TabUnderliner';
import {listItemStyle} from '../../../../app/themes';

export interface TabSettingsProps {
  useCase: string;
}

export interface TabSettingsState {
  popOverOpen?: boolean;
  popOverAnchorElement?: React.ReactInstance;
}

export class TabSettings extends React.Component<TabSettingsProps, TabSettingsState> {

  constructor(props) {
    super(props);

    this.state = {
      popOverOpen: false,
    };
  }

  render() {
    const {popOverOpen, popOverAnchorElement} = this.state;

    const onClick = (event: React.SyntheticEvent<any>): void => {
      const newState: TabSettingsState = {popOverOpen: !this.state.popOverOpen};
      if (newState.popOverOpen) {
        newState.popOverAnchorElement = event.currentTarget;
      }
      this.setState(newState);
    };

    const closePopOver = () => {
      this.setState({popOverOpen: false});
    };

    return (
      <Column className="TabSettings">
        <Row className="flex-1 Row-right">
          <IconMore onClick={onClick}/>
          <Popover
            open={popOverOpen}
            anchorEl={popOverAnchorElement}
            anchorOrigin={{horizontal: 'left', vertical: 'bottom'}}
            targetOrigin={{horizontal: 'right', vertical: 'top'}}
            onRequestClose={closePopOver}
          >
            <Menu>
              <MenuItem style={listItemStyle}>Export to Excel (.csv)</MenuItem>
              <MenuItem style={listItemStyle}>Export to JSON</MenuItem>
            </Menu>
          </Popover>
        </Row>
        <TabUnderline/>
      </Column>
    );
  }
}
