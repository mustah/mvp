import * as React from 'react';
import {Link} from 'react-router-dom';
import {routes} from '../../app/routes';
import {translate} from '../../services/translationService';
import {OnClick, OnClickWithId, RenderFunction, uuid} from '../../types/Types';
import {ActionMenuItem} from './ActionMenuItem';
import {ActionsDropdown} from './ActionsDropdown';

interface Props {
  id: uuid;
  confirmDelete: OnClickWithId;
}

export const UserActions = ({id, confirmDelete}: Props) => {

  const openAlert = () => confirmDelete(id);

  const renderPopoverContent: RenderFunction<OnClick> = (onClick: OnClick) => {
    const onClickDelete = () => {
      onClick();
      openAlert();
    };
    return [
      (
        <Link to={`${routes.adminUsersModify}/${id}`} className="link" key={`edit-${id}`}>
          <ActionMenuItem name={translate('edit user')} onClick={onClick}/>
        </Link>
      ),
      (
        <ActionMenuItem name={translate('delete user')} onClick={onClickDelete} key={`delete-${id}`}/>
      ),
    ];
  };

  return (<ActionsDropdown renderPopoverContent={renderPopoverContent}/>);
};