import ActionDelete from 'material-ui/svg-icons/action/delete';
import ImageEdit from 'material-ui/svg-icons/image/edit';
import * as React from 'react';
import {Link} from 'react-router-dom';
import {routes} from '../../../app/routes';
import {actionMenuItemIconStyle} from '../../../app/themes';
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
          <ActionMenuItem
            leftIcon={<ImageEdit style={actionMenuItemIconStyle}/>}
            name={translate('edit user')}
            onClick={onClick}
          />
        </Link>
      ),
      (
        <ActionMenuItem
          leftIcon={<ActionDelete style={actionMenuItemIconStyle}/>}
          name={translate('delete user')}
          onClick={onClickDelete}
          key={`delete-${id}`}
        />
      ),
    ];
  };

  return (<ActionsDropdown renderPopoverContent={renderPopoverContent}/>);
};
