import MenuItem from 'material-ui/MenuItem';
import * as React from 'react';
import {Link} from 'react-router-dom';
import {routes} from '../../../../app/routes';
import {menuItemInnerDivStyle} from '../../../../app/themes';
import {IconAvatar} from '../../../../components/icons/IconAvatar';
import {Column, ColumnCenter} from '../../../../components/layouts/column/Column';
import {Row, RowCenter} from '../../../../components/layouts/row/Row';
import {PopoverMenu} from '../../../../components/popover/PopoverMenu';
import {Xsmall} from '../../../../components/texts/Texts';
import {translate} from '../../../../services/translationService';
import {User} from '../../../../state/domain-models/user/userModels';
import {Clickable, OnClick, RenderFunction, uuid} from '../../../../types/Types';
import './Profile.scss';

interface Props {
  user: User;
  logout: (organisationId: uuid) => void;
}

export const Profile = (props: Props) => {
  const {user, logout} = props;
  const logoutClick = () => logout(user.organisation.code);
  const Icon = iconComponent({name: user.name});
  const renderPopoverContent: RenderFunction<OnClick> = () => ([(
      <Link to={routes.userProfile} className="link" key="goToProfile">
        <MenuItem
          style={menuItemInnerDivStyle}
          className="first-uppercase"
        >
          {translate('profile')}
        </MenuItem>
      </Link>), (
      <MenuItem style={menuItemInnerDivStyle} className="first-uppercase" onClick={logoutClick} key="logout">
        {translate('logout')}
      </MenuItem>),
    ]
  );

  return (
    <Column className="ProfileWrapper">
      <Row className="Profile">
        <PopoverMenu IconComponent={Icon} renderPopoverContent={renderPopoverContent}/>
      </Row>
    </Column>
  );
};

const iconComponent = ({name}: {name: string}) => (
  (props: Clickable) => (
    <ColumnCenter {...props} className="clickable">
      <RowCenter>
        <IconAvatar {...props} />
      </RowCenter>
      <Xsmall className="Bold">{name}</Xsmall>
    </ColumnCenter>
  )
);
