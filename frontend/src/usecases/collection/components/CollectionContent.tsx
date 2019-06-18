import * as React from 'react';
import {Column} from '../../../components/layouts/column/Column';
import {ToolbarView, ToolbarViewSettingsProps} from '../../../state/ui/toolbar/toolbarModels';
import {CollectionGraphContainer} from '../containers/CollectionGraphContainer';
import {CollectionListContainer} from '../containers/CollectionListContainer';
import {CollectionToolbarContainer} from '../containers/CollectionToolbarContainer';

const isVisible = (show: boolean): string => show ? 'flex' : 'none';

export const CollectionContent = ({view}: ToolbarViewSettingsProps) => (
  <Column>
    <CollectionToolbarContainer/>
    <Column style={{display: isVisible(view === ToolbarView.graph)}}>
      <CollectionGraphContainer/>
    </Column>
    <Column style={{display: isVisible(view === ToolbarView.table)}}>
      <CollectionListContainer/>
    </Column>
  </Column>
);
