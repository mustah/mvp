import Divider from 'material-ui/Divider';
import ActionDelete from 'material-ui/svg-icons/action/delete';
import NotificationSync from 'material-ui/svg-icons/notification/sync';
import * as React from 'react';
import {branch, renderNothing} from 'recompose';
import {actionMenuItemIconStyle, dividerStyle} from '../../app/themes';
import {isDefined} from '../../helpers/commonHelpers';
import {translate} from '../../services/translationService';
import {Meter} from '../../state/domain-models-paginated/meter/meterModels';
import {LegendItem} from '../../state/report/reportModels';
import {OnClick, OnClickWith, OnClickWithId, RenderFunction, Styled} from '../../types/Types';
import {toLegendItem} from '../../usecases/report/helpers/legendHelper';
import {withSuperAdminOnly} from '../hoc/withRoles';
import {IconReport} from '../icons/IconReport';
import {StoreProvider} from '../popover/StoreProvider';
import {ActionMenuItem, ActionMenuItemProps} from './ActionMenuItem';
import {ActionsDropdown} from './ActionsDropdown';

const deleteDividerStyle: React.CSSProperties = {
  ...dividerStyle,
  marginTop: 6,
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

type DeleteMeterMenuItemProps = ActionMenuItemProps & DeleteMeter;

const withDeleteMeterActionButton = branch<DeleteMeterMenuItemProps>(
  ({deleteMeter}) => isDefined(deleteMeter), withSuperAdminOnly, renderNothing);

const SyncWithMeteringMenuItem = withSuperAdminOnly<ActionMenuItemProps>(ActionMenuItem);
const DeleteMeterActionMenuItem = withDeleteMeterActionButton(ActionMenuItem);
const DeleteDivider = withDeleteMeterActionButton(({style}: Styled) => <Divider style={style}/>);

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
      leftIcon: <NotificationSync style={actionMenuItemIconStyle}/>,
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
        <StoreProvider key={`sync-${id}`}>
          <SyncWithMeteringMenuItem{...syncMenuItemProps}/>
        </StoreProvider>
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
        <StoreProvider key={`list-divider-${id}`}>
          <DeleteDivider {...deleteMenuItemProps} style={deleteDividerStyle}/>
        </StoreProvider>
      ),
      (
        <StoreProvider key={`delete-meter-${id}`}>
          <DeleteMeterActionMenuItem
            leftIcon={<ActionDelete style={actionMenuItemIconStyle}/>}
            {...deleteMenuItemProps}
          />
        </StoreProvider>
      ),
    ]);
  };

  return (<ActionsDropdown renderPopoverContent={renderPopoverContent}/>);
};
