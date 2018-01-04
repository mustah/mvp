import ActionSupervisorAccount from 'material-ui/svg-icons/action/supervisor-account';
import * as React from 'react';
import {connect} from 'react-redux';
import {Link} from 'react-router-dom';
import {routes} from '../../../app/routes';
import {colors, iconStyle} from '../../../app/themes';
import {Column} from '../../../components/layouts/column/Column';
import {RootState} from '../../../reducers/rootReducer';
import {getPathname} from '../../../selectors/routerSelectors';
import {translate} from '../../../services/translationService';
import {MainMenuWrapper} from '../components/main-menu-wrapper/MainMenuWrapper';
import {MenuItem} from '../components/menuitems/MenuItem';

interface StateToProps {
  pathname: string;
}

const AdminMainMenu = ({pathname}: StateToProps) => {

  return (
    <MainMenuWrapper>
        <Column>
          <Link to={routes.admin} className="link">
            <MenuItem
              name={translate('users')}
              isSelected={routes.admin === pathname}
              icon={<ActionSupervisorAccount style={iconStyle} color={colors.white} className="MenuItem-icon"/>}
            />
          </Link>
        </Column>
    </MainMenuWrapper>
  );
};

const mapStateToProps = ({routing}: RootState): StateToProps => {
  return {
    pathname: getPathname(routing),
  };
};

export const AdminMainMenuContainer =
  connect<StateToProps>(mapStateToProps)(AdminMainMenu);
