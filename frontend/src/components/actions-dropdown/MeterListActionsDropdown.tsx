import NotificationSync from 'material-ui/svg-icons/notification/sync';
import * as React from 'react';
import {actionMenuItemIconStyle} from '../../app/themes';
import {translate} from '../../services/translationService';
import {OnClick, RenderFunction} from '../../types/Types';
import {connectedSuperAdminOnly} from '../hoc/withRoles';
import {MeterListActionDropdownProps} from '../meters/MeterListContent';
import {ActionMenuItem, ActionMenuItemProps} from './ActionMenuItem';
import {ActionsDropdown} from './ActionsDropdown';

const SyncWithMeteringMenuItem = connectedSuperAdminOnly<ActionMenuItemProps>(ActionMenuItem);

export const MeterListActionsDropdown =
  ({syncMeters}: MeterListActionDropdownProps) => {

    const renderPopoverContent: RenderFunction<OnClick> = (onClick: OnClick) => {
      const syncMetersProps: ActionMenuItemProps = {
        name: translate('sync all meters on this page'),
        onClick: () => {
          onClick();
          syncMeters();
        },
      };

      return ([
        (
          <SyncWithMeteringMenuItem
            {...syncMetersProps}
            leftIcon={<NotificationSync style={actionMenuItemIconStyle}/>}
            key="sync-meters-menu-item"
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
