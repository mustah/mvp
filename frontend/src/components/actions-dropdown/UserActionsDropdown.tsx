import * as React from 'react';
import {translate} from '../../services/translationService';
import {OnClickWithId, uuid} from '../../types/Types';
import {ActionMenuItem} from './ActionMenuItem';
import {ActionsDropdown, MenuItems} from './ActionsDropdown';

interface Props {
  id: uuid;
  deleteUser: OnClickWithId;
}

export const UserActionsDropdown = ({id, deleteUser}: Props) => {
  const noop = () => null;

  const proxiedDelete = () => deleteUser(id);

  const menuItems: MenuItems = [
    <ActionMenuItem name={translate('edit user')} onClick={noop} key={`0-${id}`}/>,
    <ActionMenuItem name={translate('delete user')} onClick={proxiedDelete} key={`1-${id}`}/>,
  ];

  return (<ActionsDropdown menuItems={menuItems}/>);
};
