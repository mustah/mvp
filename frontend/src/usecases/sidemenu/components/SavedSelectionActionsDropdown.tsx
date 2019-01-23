import ActionDelete from 'material-ui/svg-icons/action/delete';
import ImageEdit from 'material-ui/svg-icons/image/edit';
import * as React from 'react';
import {Link} from 'react-router-dom';
import {routes} from '../../../app/routes';
import {actionMenuItemIconStyle} from '../../../app/themes';
import {ActionMenuItem} from '../../../components/actions-dropdown/ActionMenuItem';
import {ActionsDropdown} from '../../../components/actions-dropdown/ActionsDropdown';
import {IconReport} from '../../../components/icons/IconReport';
import {translate} from '../../../services/translationService';
import {Callback, OnClick, OnClickWithId, RenderFunction, uuid} from '../../../types/Types';

interface Props {
  id: uuid;
  confirmDelete: OnClickWithId;
  onSelectSelection: Callback;
  onAddAllToReport: Callback;
}

export const SavedSelectionActionsDropdown = ({id, confirmDelete, onAddAllToReport, onSelectSelection}: Props) => {

  const renderPopoverContent: RenderFunction<OnClick> = (onClick: OnClick) => {
    const onClickEdit = () => {
      onClick();
      onSelectSelection();
    };
    const onClickAddAllToReport = () => {
      onClick();
      onAddAllToReport();
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
          leftIcon={<IconReport style={actionMenuItemIconStyle}/>}
          name={translate('show all in report')}
          onClick={onClickAddAllToReport}
          key={`add-all-to-report-${id}`}
        />
      ),
      (
        <ActionMenuItem
          leftIcon={<ActionDelete style={actionMenuItemIconStyle}/>}
          name={translate('delete user selection')}
          onClick={onClickDelete}
          key={`delete-user-selection-${id}`}
        />
      )
    ];
  };

  return <ActionsDropdown renderPopoverContent={renderPopoverContent}/>;
};
