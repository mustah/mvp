import Divider from 'material-ui/Divider';
import ActionDelete from 'material-ui/svg-icons/action/delete';
import NotificationSync from 'material-ui/svg-icons/notification/sync';
import * as React from 'react';
import {DispatchProp} from 'react-redux';
import {branch, renderNothing} from 'recompose';
import {actionMenuItemIconStyle, dividerStyle} from '../../app/themes';
import {isDefined} from '../../helpers/commonHelpers';
import {translate} from '../../services/translationService';
import {Meter} from '../../state/domain-models-paginated/meter/meterModels';
import {LegendItem} from '../../state/report/reportModels';
import {OnClick, OnClickWith, OnClickWithId, RenderFunction} from '../../types/Types';
import {toLegendItem} from '../../usecases/report/helpers/legendHelper';
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
  item: Meter;
  addToReport: OnClickWith<LegendItem>;
  syncWithMetering?: OnClickWithId;
}

const MyDivider = ({deleteMeter, dispatch, ...otherProps}: DeleteMeterMenuItemProps) => <Divider {...otherProps}/>;

type DeleteMeterMenuItemProps = ActionMenuItemProps & DeleteMeter & DispatchProp<any>;

const withDeleteMeterActionButton = branch<DeleteMeterMenuItemProps>(
  ({deleteMeter}) => isDefined(deleteMeter), connectedSuperAdminOnly, renderNothing);

const SyncWithMeteringMenuItem = connectedSuperAdminOnly<ActionMenuItemProps>(ActionMenuItem);
const DeleteMeterActionMenuItem = withDeleteMeterActionButton(ActionMenuItem);
const DeleteDivider = withDeleteMeterActionButton(MyDivider);

export const ListActionsDropdown = ({item, deleteMeter, addToReport, syncWithMetering}: Props) => {
  const {id} = item;

  const renderPopoverContent: RenderFunction<OnClick> = (onClick: OnClick) => {
    const onAddToReport = () => {
      onClick();
      addToReport(toLegendItem(item));
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
        />
      ),
    ]);
  };

  return (<ActionsDropdown renderPopoverContent={renderPopoverContent}/>);
};
