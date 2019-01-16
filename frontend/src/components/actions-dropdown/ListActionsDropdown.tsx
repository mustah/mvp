import Divider from 'material-ui/Divider';
import EditorInsertChart from 'material-ui/svg-icons/editor/insert-chart';
import NotificationSync from 'material-ui/svg-icons/notification/sync';
import * as React from 'react';
import {branch, renderNothing} from 'recompose';
import {routes} from '../../app/routes';
import {actionMenuItemIconStyle} from '../../app/themes';
import {isDefined} from '../../helpers/commonUtils';
import {history} from '../../index';
import {translate} from '../../services/translationService';
import {IdNamed, OnClick, OnClickWithId, RenderFunction} from '../../types/Types';
import {connectedSuperAdminOnly} from '../hoc/withRoles';
import {ActionMenuItem, ActionMenuItemProps} from './ActionMenuItem';
import {ActionsDropdown} from './ActionsDropdown';

interface DeleteMeter {
  deleteMeter?: OnClickWithId;
}

interface Props extends DeleteMeter {
  item: IdNamed;
  selectEntryAdd: OnClickWithId;
  syncWithMetering?: OnClickWithId;
}

type DeleteMeterMenuItemProps = ActionMenuItemProps & DeleteMeter;

const withDeleteMeterActionButton = branch<DeleteMeterMenuItemProps>(
  ({deleteMeter}) => isDefined(deleteMeter), connectedSuperAdminOnly, renderNothing);

const SyncWithMeteringMenuItem = connectedSuperAdminOnly<ActionMenuItemProps>(ActionMenuItem);
const DeleteMeterActionMenuItem = withDeleteMeterActionButton(ActionMenuItem);

export const ListActionsDropdown = ({item: {id}, deleteMeter, selectEntryAdd, syncWithMetering}: Props) => {

  const renderPopoverContent: RenderFunction<OnClick> = (onClick: OnClick) => {
    const onAddToReport = () => {
      onClick();
      history.push(`${routes.report}/${id}`);
      selectEntryAdd(id);
    };

    const syncMenuItemProps: ActionMenuItemProps = {
      name: translate('sync'),
      onClick: () => {
        onClick();
        if (syncWithMetering) {
          syncWithMetering(id);
        }
      },
    };

    const deleteMenuItemProps: DeleteMeterMenuItemProps = {
      name: translate('delete meter'),
      deleteMeter,
      onClick: () => {
        onClick();
        deleteMeter!(id);
      },
    };

    return ([
      (
        <SyncWithMeteringMenuItem
          {...syncMenuItemProps}
          key={`sync-${id}`}
          leftIcon={<NotificationSync style={actionMenuItemIconStyle}/>}
        />
      ),
      (
        <ActionMenuItem
          leftIcon={<EditorInsertChart style={actionMenuItemIconStyle}/>}
          name={translate('add to report')}
          onClick={onAddToReport}
          key={`add-to-report-${id}`}
        />
      ),
      <Divider key={`list-divider-${id}`}/>,
      <DeleteMeterActionMenuItem {...deleteMenuItemProps} key={`delete-meter-${id}`}/>
    ]);
  };

  return (<ActionsDropdown renderPopoverContent={renderPopoverContent}/>);
};
