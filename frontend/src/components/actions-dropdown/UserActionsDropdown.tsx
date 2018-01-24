import * as React from 'react';
import {Link} from 'react-router-dom';
import {routes} from '../../app/routes';
import {translate} from '../../services/translationService';
import {OnClickWithId, RenderFunction, uuid} from '../../types/Types';
import {ActionMenuItem} from './ActionMenuItem';
import {ActionsDropdown} from './ActionsDropdown';

interface Props {
  id: uuid;
  openDeleteAlert: OnClickWithId;
}

export const UserActionsDropdown = ({id, openDeleteAlert}: Props) => {

  const openAlert = () => openDeleteAlert(id);

  const renderPopoverContent: RenderFunction = (onClick) => {
    const onClickDelete = () => {
      onClick();
      openAlert();
    };
    return [(
      <Link to={`${routes.adminUsersModify}/${id}`} className="link" key={`0-${id}`}>
        <ActionMenuItem name={translate('edit user')} onClick={onClick}/>
      </Link>),
      <ActionMenuItem name={translate('delete user')} onClick={onClickDelete} key={`1-${id}`}/>,
    ];
  };

  return (<ActionsDropdown renderPopoverContent={renderPopoverContent}/>);
};
