import Divider from 'material-ui/Divider';
import ActionDelete from 'material-ui/svg-icons/action/delete';
import ImageEdit from 'material-ui/svg-icons/image/edit';
import * as React from 'react';
import {routes} from '../../../app/routes';
import {actionMenuItemIconStyle, dividerStyle} from '../../../app/themes';
import {ActionMenuItem} from '../../../components/actions-dropdown/ActionMenuItem';
import {ActionsDropdown} from '../../../components/actions-dropdown/ActionsDropdown';
import {IconReport} from '../../../components/icons/IconReport';
import {Link} from '../../../components/links/Link';
import {translate} from '../../../services/translationService';
import {initialSelectionId} from '../../../state/user-selection/userSelectionModels';
import {Callback, OnClick, OnClickWithId, RenderFunction, uuid} from '../../../types/Types';

export interface DispatchProps {
  confirmDelete: OnClickWithId;
  onEditSelection: Callback;
  onShowAverageInReport: Callback;
}

export interface ActionDropdownProps extends DispatchProps {
  id: uuid;
}

export const SavedSelectionActionsDropdown =
  ({id, confirmDelete, onShowAverageInReport, onEditSelection}: ActionDropdownProps) => {

    const renderPopoverContent: RenderFunction<OnClick> = (onClick: OnClick) => {
      const onClickEditSelection = () => {
        onClick();
        onEditSelection();
      };
      const onClickShowAverageInReport = () => {
        onClick();
        onShowAverageInReport();
      };
      const onClickDelete = () => {
        onClick();
        confirmDelete(id);
      };

      const actionMenuItems = [
        (
          <Link to={routes.selection} key={`edit-user-selection-${id}`}>
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
          (
            <ActionMenuItem
              leftIcon={<IconReport style={actionMenuItemIconStyle}/>}
              name={translate('show average in report')}
              onClick={onClickShowAverageInReport}
              key={`show-average-in-report-${id}`}
            />
          ),
          (
            <Divider style={dividerStyle} key={`user-selection-divider-${id}`}/>
          ),
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
