import ActionDelete from 'material-ui/svg-icons/action/delete';
import ImageEdit from 'material-ui/svg-icons/image/edit';
import * as React from 'react';
import {Link} from 'react-router-dom';
import {routes} from '../../../app/routes';
import {actionMenuItemIconStyle} from '../../../app/themes';
import {ActionMenuItem} from '../../../components/actions-dropdown/ActionMenuItem';
import {ActionsDropdown} from '../../../components/actions-dropdown/ActionsDropdown';
import {translate} from '../../../services/translationService';
import {Callback, OnClick, OnClickWithId, RenderFunction, uuid} from '../../../types/Types';

interface Props {
  id: uuid;
  confirmDelete: OnClickWithId;
  onSelectSelection: Callback;
}

export const SavedSelectionActionsDropdown = ({id, confirmDelete, onSelectSelection}: Props) => {

  const renderPopoverContent: RenderFunction<OnClick> = (onClick: OnClick) => {
    const onClickEdit = () => {
      onClick();
      onSelectSelection();
    };
    const onClickDelete = () => {
      onClick();
      confirmDelete(id);
    };
    return [
      (
        <Link to={`${routes.selection}`} className="link" key={`edit-user-selection-${id}`}>
          <ActionMenuItem
            leftIcon={<ImageEdit style={actionMenuItemIconStyle}/>}
            name={translate('edit user selection')}
            onClick={onClickEdit}
          />
        </Link>
      ),
      (
        <ActionMenuItem
          leftIcon={<ActionDelete style={actionMenuItemIconStyle}/>}
          name={translate('delete user selection')}
          onClick={onClickDelete}
          key={`1-${id}`}
        />
      )
    ];
  };

  return <ActionsDropdown renderPopoverContent={renderPopoverContent}/>;
};
