import * as React from 'react';
import {routes} from '../../../app/routes';
import '../../../components/actions-dropdown/ActionsDropdown.scss';
import {ButtonAdd} from '../../../components/buttons/ButtonAdd';
import {Link} from '../../../components/links/Link';
import {translate} from '../../../services/translationService';

export const AddUserButton = () => (
  <Link to={routes.adminUsersAdd} key={'add user'}>
    <ButtonAdd label={translate('add user')}/>
  </Link>
);
