import AppBar from 'material-ui/AppBar';
import Drawer from 'material-ui/Drawer';
import List from 'material-ui/List/List';
import ListItem from 'material-ui/List/ListItem';
import ToggleStar from 'material-ui/svg-icons/toggle/star';
import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/index';
import {translate} from '../../../services/translationService';
import {drawerWidth} from '../../app/themes';
import {NavigationMenuIcon} from '../../common/components/icons/NavigationMenuIcon';
import {toggleShowHideSideMenu} from '../sideMenuActions';
import {SideMenuState} from '../sideMenuReducer';

interface SideMenuContainerProps {
  sideMenu: SideMenuState;
  toggleShowHideSideMenu: () => void;
}

const SideMenuContainer = (props: SideMenuContainerProps) => {
  const {sideMenu: {isOpen}} = props;

  const listItems = [
    <ListItem primaryText="Göteborg - Centrum" key={1}/>,
    <ListItem primaryText="Gateways med fel" key={2}/>,
  ];

  return (
    <Drawer open={isOpen} docked={true} containerStyle={{left: isOpen ? drawerWidth : 0}}>
      <AppBar
        title="MVP"
        iconElementRight={<NavigationMenuIcon onClick={props.toggleShowHideSideMenu}/>}
        showMenuIconButton={false}
      />
      <List>
        <ListItem
          className="ListItem"
          primaryText={translate('saved search')}
          leftIcon={<ToggleStar/>}
          initiallyOpen={true}
          nestedItems={listItems}
        />
      </List>
    </Drawer>
  );
};

const mapStateToProps = (state: RootState) => {
  const {ui: {sideMenu}} = state;
  return {
    sideMenu,
  };
};

const mapDispatchToProps = dispatch => bindActionCreators({
  toggleShowHideSideMenu,
}, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(SideMenuContainer);
