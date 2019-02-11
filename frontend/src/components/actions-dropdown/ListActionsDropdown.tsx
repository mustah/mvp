import Divider from 'material-ui/Divider';
import ActionDelete from 'material-ui/svg-icons/action/delete';
import NotificationSync from 'material-ui/svg-icons/notification/sync';
import * as React from 'react';
import {DispatchProp} from 'react-redux';
import {branch, renderNothing} from 'recompose';
import {routes} from '../../app/routes';
import {actionMenuItemIconStyle, dividerStyle} from '../../app/themes';
import {isDefined} from '../../helpers/commonUtils';
import {history} from '../../index';
import {translate} from '../../services/translationService';
import {IdNamed, OnClick, OnClickWithId, RenderFunction} from '../../types/Types';
import {connectedSuperAdminOnly} from '../hoc/withRoles';
import {IconReport} from '../icons/IconReport';
import {ActionMenuItem, ActionMenuItemProps} from './ActionMenuItem';
import {ActionsDropdown} from './ActionsDropdown';

const deleteDividerStyle: React.CSSProperties = {
  ...dividerStyle,
  marginBottom: 6,
};

interface DeleteMeter {
  deleteMeter?: OnClickWithId;
}

interface Props extends DeleteMeter {
  item: IdNamed;
  selectEntryAdd: OnClickWithId;
  syncWithMetering?: OnClickWithId;
}

const MyDivider = ({deleteMeter, dispatch, ...otherProps}: DeleteMeterMenuItemProps) => <Divider {...otherProps}/>;

type DeleteMeterMenuItemProps = ActionMenuItemProps & DeleteMeter & DispatchProp<any>;

const withDeleteMeterActionButton = branch<DeleteMeterMenuItemProps>(
  ({deleteMeter}) => isDefined(deleteMeter), connectedSuperAdminOnly, renderNothing);

const SyncWithMeteringMenuItem = connectedSuperAdminOnly<ActionMenuItemProps>(ActionMenuItem);
const DeleteMeterActionMenuItem = withDeleteMeterActionButton(ActionMenuItem);
const DeleteDivider = withDeleteMeterActionButton(MyDivider);

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
          leftIcon={<IconReport style={actionMenuItemIconStyle}/>}
          name={translate('add to report')}
          onClick={onAddToReport}
          key={`add-to-report-${id}`}
        />
      ),
      (
        <DeleteDivider
          {...deleteMenuItemProps}
          style={deleteDividerStyle}
          key={`list-divider-${id}`}
        />
      ),
      (
        <DeleteMeterActionMenuItem
          leftIcon={<ActionDelete style={actionMenuItemIconStyle}/>}
          {...deleteMenuItemProps}
          key={`delete-meter-${id}`}
        />)
    ]);
  };

  return (<ActionsDropdown renderPopoverContent={renderPopoverContent}/>);
};
