import * as React from 'react';
import {Link} from 'react-router-dom';
import {routes} from '../../app/routes';
import {translate} from '../../services/translationService';
import {OnClickWithId, uuid} from '../../types/Types';
import {ActionMenuItem} from './ActionMenuItem';
import {ActionsDropdown, MenuItems} from './ActionsDropdown';

interface Props {
  id: uuid;
  deleteUser: OnClickWithId;
}

export const UserActionsDropdown = ({id, deleteUser}: Props) => {

  const proxiedDelete = () => deleteUser(id);

  const menuItems: MenuItems = [(
    <Link to={`${routes.adminUsersModify}/${id}`} className="link" key={`0-${id}`}>
      <ActionMenuItem name={translate('edit user')}/>
    </Link>),
    <ActionMenuItem name={translate('delete user')} onClick={proxiedDelete} key={`1-${id}`}/>,
  ];

  return (<ActionsDropdown menuItems={menuItems}/>);
};
