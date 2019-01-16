import * as React from 'react';
import {Link} from 'react-router-dom';
import {routes} from '../../../app/routes';
import {ActionMenuItem} from '../../../components/actions-dropdown/ActionMenuItem';
import {ActionsDropdown} from '../../../components/actions-dropdown/ActionsDropdown';
import {translate} from '../../../services/translationService';
import {OnClick, OnClickWithId, RenderFunction, uuid} from '../../../types/Types';

interface Props {
  id: uuid;
  confirmDelete: OnClickWithId;
}

export const UserActions = ({id, confirmDelete}: Props) => {

  const renderPopoverContent: RenderFunction<OnClick> = (onClick: OnClick) => {
    const onClickDelete = () => {
      onClick();
      confirmDelete(id);
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
