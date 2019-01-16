import * as React from 'react';
import {Link} from 'react-router-dom';
import {routes} from '../../../app/routes';
import '../../../components/actions-dropdown/ActionsDropdown.scss';
import {ButtonAdd} from '../../../components/buttons/ButtonAdd';
import {translate} from '../../../services/translationService';

export const AddUserButton = () => (
  <Link to={routes.adminUsersAdd} className="link" key={'add user'}>
    <ButtonAdd label={translate('add user')}/>
  </Link>
);
