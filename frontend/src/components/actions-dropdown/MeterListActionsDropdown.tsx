import * as React from 'react';
import {routes} from '../../app/routes';
import {history} from '../../index';
import {translate} from '../../services/translationService';
import {OnClick, RenderFunction} from '../../types/Types';
import {connectedSuperAdminOnly} from '../hoc/withRoles';
import {MeterListActionDropdownProps} from '../meters/MeterListContent';
import {ActionMenuItem, ActionMenuItemProps} from './ActionMenuItem';
import {ActionsDropdown} from './ActionsDropdown';

const SyncWithMeteringMenuItem = connectedSuperAdminOnly<ActionMenuItemProps>(ActionMenuItem);

export const MeterListActionsDropdown =
  ({syncMeters, showMetersInGraph}: MeterListActionDropdownProps) => {
    const renderPopoverContent: RenderFunction<OnClick> = (onClick: OnClick) => {
      const syncMetersProps: ActionMenuItemProps = {
        name: translate('sync all meters on this page'),
        onClick: () => {
          onClick();
          syncMeters();
        },
      };

      const showMetersInGraphProps: ActionMenuItemProps = {
        name: translate('add all on this page to report'),
        onClick: () => {
          onClick();
          history.push(`${routes.report}/`);
          showMetersInGraph();
        },
      };
      return ([
        <SyncWithMeteringMenuItem {...syncMetersProps} key="sync-meters-menu-item"/>,
        <ActionMenuItem {...showMetersInGraphProps} key="show-meters-in-graph-menu-item"/>,
      ]);
    };

    return (
      <ActionsDropdown
        renderPopoverContent={renderPopoverContent}
        className="SelectionResultActionDropdown"
      />);
  };
