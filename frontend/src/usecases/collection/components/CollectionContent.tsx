import * as React from 'react';
import {Column} from '../../../components/layouts/column/Column';
import {NumItems} from '../../../components/lists/NumItems';
import {translate} from '../../../services/translationService';
import {TotalElements} from '../../../state/domain-models-paginated/paginatedDomainModels';
import {ToolbarView, ToolbarViewSettingsProps} from '../../../state/ui/toolbar/toolbarModels';
import {HasContent} from '../../../types/Types';
import {CollectionGraphContainer} from '../containers/CollectionGraphContainer';
import {CollectionListContainer} from '../containers/CollectionListContainer';
import {CollectionToolbarContainer} from '../containers/CollectionToolbarContainer';

const isVisible = (show: boolean): string => show ? 'flex' : 'none';

export type Props = ToolbarViewSettingsProps & HasContent & TotalElements;

export const CollectionContent = ({view, ...numItemsProps}: Props) => (
  <Column>
    <CollectionToolbarContainer/>
    <Column style={{display: isVisible(view === ToolbarView.graph)}}>
      <CollectionGraphContainer/>
    </Column>
    <Column style={{display: isVisible(view === ToolbarView.table)}}>
      <CollectionListContainer paddingBottom={296}/>
    </Column>
    <Column style={{display: isVisible(view === ToolbarView.table)}}>
      <NumItems label={translate('total facilities')} {...numItemsProps}/>
    </Column>
  </Column>
);
