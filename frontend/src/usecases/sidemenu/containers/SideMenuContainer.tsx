import AppBar from 'material-ui/AppBar';
import Drawer from 'material-ui/Drawer';
import List from 'material-ui/List/List';
import ListItem from 'material-ui/List/ListItem';
import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import 'SideMenuContainer.scss';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {isSideMenuOpen} from '../../../state/ui/uiSelectors';
import {drawerWidth, sideBarHeaders} from '../../app/themes';
import {IconNavigationMenu} from '../../common/components/icons/IconNavigationMenu';
import {SelectionTree} from '../components/collapsibleMenuEntry/SelectionTree';
import {toggleShowHideSideMenu} from '../sideMenuActions';

interface StateToProps {
  isSideMenuOpen: boolean;
}

interface DispatchToProps {
  toggleShowHideSideMenu: () => void;
}

const SideMenuContainerComponent = (props: StateToProps & DispatchToProps) => {
  const {isSideMenuOpen} = props;
  const listItems = [
    <ListItem primaryText="Göteborg - Centrum" key={1}/>,
    <ListItem primaryText="Gateways med fel" key={2}/>,
  ];

  return (
    <Drawer open={isSideMenuOpen} docked={true} containerStyle={{left: isSideMenuOpen ? drawerWidth : 0}}>
      <AppBar
        className="AppTitle"
        title={translate('metering')}
        iconElementRight={<IconNavigationMenu onClick={props.toggleShowHideSideMenu}/>}
        showMenuIconButton={false}
      />
      <List>
        <ListItem
          className="ListItem"
          primaryText={translate('saved search')}
          initiallyOpen={true}
          style={sideBarHeaders.fontStyle}
          nestedItems={listItems}
        />
      </List>
      <SelectionTree topLevel={'cities'}/>
    </Drawer>
  );
};

const mapStateToProps = ({ui}: RootState): StateToProps => {
  return {
    isSideMenuOpen: isSideMenuOpen(ui),
  };
};

const mapDispatchToProps = dispatch => bindActionCreators({
  toggleShowHideSideMenu,
}, dispatch);

export const SideMenuContainer =
  connect<StateToProps, DispatchToProps, {}>(mapStateToProps, mapDispatchToProps)(SideMenuContainerComponent);
