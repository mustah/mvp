import * as React from 'react';
import {Column} from '../../../../components/layouts/column/Column';
import {ToolbarView} from '../../../../state/ui/toolbar/toolbarModels';
import {MeterDetailProps} from '../../measurements/meterDetailModels';
import {CollectionGraphContainer} from '../containers/CollectionGraphContainer';
import {CollectionListContainer} from '../containers/CollectionListContainer';
import {CollectionToolbarContainer} from '../containers/CollectionToolbarContainer';

const isVisible = (show: boolean): string => show ? 'flex' : 'none';

export const CollectionContent = ({view, meter}: MeterDetailProps) => (
  <Column>
    <CollectionToolbarContainer/>
    <Column style={{display: isVisible(view === ToolbarView.graph)}}>
      <CollectionGraphContainer meterId={meter.id}/>
    </Column>
    <Column style={{display: isVisible(view === ToolbarView.table)}}>
      <CollectionListContainer meterId={meter.id}/>
    </Column>
  </Column>
);
