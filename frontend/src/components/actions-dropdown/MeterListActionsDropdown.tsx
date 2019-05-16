import NotificationSync from 'material-ui/svg-icons/notification/sync';
import * as React from 'react';
import {actionMenuItemIconStyle} from '../../app/themes';
import {translate} from '../../services/translationService';
import {OnClick, RenderFunction} from '../../types/Types';
import {withSuperAdminOnly} from '../hoc/withRoles';
import {IconReport} from '../icons/IconReport';
import {MeterListActionDropdownProps} from '../meters/MeterListContent';
import {ActionMenuItem, ActionMenuItemProps} from './ActionMenuItem';
import {ActionsDropdown} from './ActionsDropdown';

const SyncWithMeteringMenuItem = withSuperAdminOnly<ActionMenuItemProps>(ActionMenuItem);

export const MeterListActionsDropdown =
  ({addAllToReport, syncMeters}: MeterListActionDropdownProps) => {

    const renderPopoverContent: RenderFunction<OnClick> = (onClick: OnClick) => {
      const syncMetersProps: ActionMenuItemProps = {
        name: translate('sync all meters on this page'),
        onClick: () => {
          onClick();
          syncMeters();
        },
        leftIcon: <NotificationSync style={actionMenuItemIconStyle}/>,
      };

      const onAddAllToReport = () => {
        onClick();
        addAllToReport();
      };

      return ([
        (
          <SyncWithMeteringMenuItem {...syncMetersProps} key="sync-meters-menu-item"/>
        ),
        (
          <ActionMenuItem
            leftIcon={<IconReport style={actionMenuItemIconStyle}/>}
            name={translate('add all to report')}
            onClick={onAddAllToReport}
            key="add-all-to-report"
          />
        ),
      ]);
    };

    return (
      <ActionsDropdown
        renderPopoverContent={renderPopoverContent}
        className="SelectionResultActionDropdown"
      />
    );
  };
