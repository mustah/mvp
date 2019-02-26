import Divider from 'material-ui/Divider';
import ActionDelete from 'material-ui/svg-icons/action/delete';
import ImageEdit from 'material-ui/svg-icons/image/edit';
import * as React from 'react';
import {Link} from 'react-router-dom';
import {routes} from '../../../app/routes';
import {actionMenuItemIconStyle, dividerStyle} from '../../../app/themes';
import {ActionMenuItem} from '../../../components/actions-dropdown/ActionMenuItem';
import {ActionsDropdown} from '../../../components/actions-dropdown/ActionsDropdown';
import {translate} from '../../../services/translationService';
import {initialSelectionId} from '../../../state/user-selection/userSelectionModels';
import {Callback, OnClick, OnClickWithId, RenderFunction, uuid} from '../../../types/Types';

interface Props {
  id: uuid;
  confirmDelete: OnClickWithId;
  onEditSelection: Callback;
}

export const SavedSelectionActionsDropdown = ({id, confirmDelete, onEditSelection}: Props) => {

  const renderPopoverContent: RenderFunction<OnClick> = (onClick: OnClick) => {
    const onClickEditSelection = () => {
      onClick();
      onEditSelection();
    };
    const onClickDelete = () => {
      onClick();
      confirmDelete(id);
    };

    const actionMenuItems = [
      (
        <Link to={`${routes.selection}`} className="link" key={`edit-user-selection-${id}`}>
          <ActionMenuItem
            leftIcon={<ImageEdit style={actionMenuItemIconStyle}/>}
            name={translate('edit user selection')}
            onClick={onClickEditSelection}
          />
        </Link>
      ),
    ];

    return id !== initialSelectionId
      ? [
        ...actionMenuItems,
        (<Divider style={dividerStyle} key={`user-selection-divider-${id}`}/>),
        (
          <ActionMenuItem
            leftIcon={<ActionDelete style={actionMenuItemIconStyle}/>}
            name={translate('delete user selection')}
            onClick={onClickDelete}
            key={`delete-user-selection-${id}`}
          />
        )
      ]
      : actionMenuItems;
  };

  return <ActionsDropdown renderPopoverContent={renderPopoverContent}/>;
};
