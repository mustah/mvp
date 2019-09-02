import Divider from 'material-ui/Divider';
import ActionDelete from 'material-ui/svg-icons/action/delete';
import ImageEdit from 'material-ui/svg-icons/image/edit';
import * as React from 'react';
import {routes} from '../../../app/routes';
import {actionMenuItemIconStyle, dividerStyle} from '../../../app/themes';
import {ActionMenuItem} from '../../../components/actions-dropdown/ActionMenuItem';
import {ActionsDropdown} from '../../../components/actions-dropdown/ActionsDropdown';
import {Link} from '../../../components/links/Link';
import {translate} from '../../../services/translationService';
import {OnClick, OnClickWithId, RenderFunction, UseCases, uuid} from '../../../types/Types';

interface Props {
  id: uuid;
  confirmDelete: OnClickWithId;
  useCase: UseCases;
}

export const UserActions = ({id, confirmDelete, useCase}: Props) => {
  const modifyUserUrl = `${useCase === UseCases.otc ? routes.otcUsersModify : routes.adminUsersModify}/${id}`;

  const renderPopoverContent: RenderFunction<OnClick> = (onClick: OnClick) => {
    const onClickDelete = () => {
      onClick();
      confirmDelete(id);
    };
    return [
      (
        <Link to={modifyUserUrl} key={`edit-${id}`}>
          <ActionMenuItem
            leftIcon={<ImageEdit style={actionMenuItemIconStyle}/>}
            name={translate('edit user')}
            onClick={onClick}
          />
        </Link>
      ),
      (<Divider style={dividerStyle} key={`divider-user-admin-${id}`}/>),
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

  return <ActionsDropdown renderPopoverContent={renderPopoverContent}/>;
};
