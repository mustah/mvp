import 'MainMenuContainer.scss';
import ContentFilterList from 'material-ui/svg-icons/content/filter-list';
import * as React from 'react';
import {connect} from 'react-redux';
import {Link} from 'react-router-dom';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/rootReducer';
import {getPathname} from '../../../selectors/routerSelectors';
import {translate} from '../../../services/translationService';
import {isSideMenuOpen} from '../../../state/ui/uiSelectors';
import {OnClick} from '../../../types/Types';
import {routes} from '../../../app/routes';
import {colors, iconStyle} from '../../../app/themes';
import {AuthState} from '../../auth/authReducer';
import {IconCollection} from '../../../components/icons/IconCollection';
import {IconDashboard} from '../../../components/icons/IconDashboard';
import {IconReport} from '../../../components/icons/IconReport';
import {IconValidation} from '../../../components/icons/IconValidation';
import {Column} from '../../../components/layouts/column/Column';
import {toggleShowHideSideMenu} from '../../sidemenu/sideMenuActions';
import {MainMenuToggleIcon} from '../components/menuitems/MainMenuToggleIcon';
import {MenuItem} from '../components/menuitems/MenuItem';

interface StateToProps {
  pathname: string;
  auth: AuthState;
  isSideMenuOpen: boolean;
}

interface DispatchToProps {
  toggleShowHideSideMenu: OnClick;
}

const MainMenuContainerComponent = (props: StateToProps & DispatchToProps) => {
  const {pathname, auth, isSideMenuOpen} = props;

  if (!auth.isAuthenticated) {
    return null;
  }

  return (
    <Column className="MainMenuContainer">
      <Column className="MenuItems Column-space-between">
        <Column>
          <Link to={routes.selection} className="link">
            <MenuItem
              name={translate('selection')}
              isSelected={routes.selection === pathname}
              icon={<ContentFilterList style={iconStyle} color={colors.white} className="MenuItem-icon"/>}
            />
          </Link>
          <Link to={routes.dashboard} className="link">
            <MenuItem
              name={translate('dashboard')}
              isSelected={routes.dashboard === pathname || routes.home === pathname}
              icon={<IconDashboard className="MenuItem-icon"/>}
            />
          </Link>
          <Link to={routes.collection} className="link">
            <MenuItem
              name={translate('collection')}
              isSelected={routes.collection === pathname}
              icon={<IconCollection className="MenuItem-icon"/>}
            />
          </Link>
          <Link to={routes.validation} className="link">
            <MenuItem
              name={translate('validation')}
              isSelected={routes.validation === pathname}
              icon={<IconValidation className="MenuItem-icon"/>}
            />
          </Link>
          <Link to={routes.report} className="link">
            <MenuItem
              name={translate('report')}
              isSelected={pathname.startsWith(routes.report) && !pathname.includes('selection')}
              icon={<IconReport className="MenuItem-icon"/>}
            />
          </Link>

          <MainMenuToggleIcon onClick={props.toggleShowHideSideMenu} isSideMenuOpen={isSideMenuOpen}/>

        </Column>
      </Column>
    </Column>
  );
};

const mapStateToProps = ({auth, routing, ui}: RootState): StateToProps => {
  return {
    pathname: getPathname(routing),
    auth,
    isSideMenuOpen: isSideMenuOpen(ui),
  };
};

const mapDispatchToProps = (dispatch) => bindActionCreators({
  toggleShowHideSideMenu,
}, dispatch);

export const MainMenuContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(MainMenuContainerComponent);
