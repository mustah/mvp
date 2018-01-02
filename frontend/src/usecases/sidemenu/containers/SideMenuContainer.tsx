import AppBar from 'material-ui/AppBar';
import Drawer from 'material-ui/Drawer';
import * as React from 'react';
import {connect} from 'react-redux';
import {Link} from 'react-router-dom';
import {routes} from '../../../app/routes';
import {drawerWidth} from '../../../app/themes';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {isSideMenuOpen} from '../../../state/ui/uiSelectors';
import {SavedSelectionsContainer} from '../components/savedSelections/SavedSelections';
import {SelectionTreeContainer} from './selection-tree/SelectionTreeContainer';
import './SideMenuContainer.scss';

interface StateToProps {
  isSideMenuOpen: boolean;
}

const SideMenuContainerComponent = (props: StateToProps) => {
  const {isSideMenuOpen} = props;

  const containerStyle: React.CSSProperties = {left: isSideMenuOpen ? drawerWidth : 0};

  return (
    <Drawer
      containerClassName="DrawerContainer"
      open={isSideMenuOpen}
      docked={true}
      containerStyle={containerStyle}
    >
      <AppBar
        className="AppTitle"
        title={translate('metering')}
        showMenuIconButton={false}
        iconElementRight={<Link to={routes.admin}> <div>{translate('admin')}</div></Link>}
      />
      <SavedSelectionsContainer/>

      <SelectionTreeContainer topLevel={'cities'}/>
    </Drawer>
  );
};

const mapStateToProps = ({ui}: RootState): StateToProps => {
  return {
    isSideMenuOpen: isSideMenuOpen(ui),
  };
};

export const SideMenuContainer =
  connect<StateToProps>(mapStateToProps)(SideMenuContainerComponent);
