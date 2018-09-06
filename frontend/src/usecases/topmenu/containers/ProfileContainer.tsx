import MenuItem from 'material-ui/MenuItem';
import * as React from 'react';
import {connect} from 'react-redux';
import {Link} from 'react-router-dom';
import {bindActionCreators} from 'redux';
import {routes} from '../../../app/routes';
import {menuItemInnerDivStyle} from '../../../app/themes';
import {IconAvatar} from '../../../components/icons/IconAvatar';
import {Column, ColumnCenter} from '../../../components/layouts/column/Column';
import {Row, RowCenter} from '../../../components/layouts/row/Row';
import {anchorOrigin, PopoverMenu, targetOrigin} from '../../../components/popover/PopoverMenu';
import {Small} from '../../../components/texts/Texts';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {User} from '../../../state/domain-models/user/userModels';
import {Clickable, OnClick, RenderFunction} from '../../../types/Types';
import {logout} from '../../auth/authActions';
import {getUser} from '../../auth/authSelectors';
import './ProfileContainer.scss';

interface StateToProps {
  user: User;
}

interface DispatchToProps {
  logout: OnClick;
}

const makeAvatarIcon = (name: string) => (props: Clickable) => (
  <ColumnCenter {...props} className="MenuItem clickable">
    <RowCenter>
      <IconAvatar/>
    </RowCenter>
    <Small className="Username">{name}</Small>
  </ColumnCenter>
);

type Props = StateToProps & DispatchToProps;

const Profile = ({user: {name}, logout}: Props) => {
  const Icon = makeAvatarIcon(name);

  const renderPopoverContent: RenderFunction<OnClick> = () => ([
      (
        <Link to={routes.userProfile} className="link" key="goToProfile">
          <MenuItem
            style={menuItemInnerDivStyle}
            className="first-uppercase"
          >
            {translate('profile')}
          </MenuItem>
        </Link>
      ),
      (
        <MenuItem style={menuItemInnerDivStyle} className="first-uppercase" onClick={logout} key="logout">
          {translate('logout')}
        </MenuItem>
      ),
    ]
  );

  return (
    <Column className="ProfileWrapper">
      <Row className="Profile">
        <PopoverMenu
          IconComponent={Icon}
          anchorOrigin={anchorOrigin}
          targetOrigin={targetOrigin}
          renderPopoverContent={renderPopoverContent}
        />
      </Row>
    </Column>
  );
};

const mapStateToProps = ({auth}: RootState): StateToProps => ({
  user: getUser(auth),
});

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  logout,
}, dispatch);

export const ProfileContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(Profile);
