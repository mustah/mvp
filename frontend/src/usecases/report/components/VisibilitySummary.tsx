import {difference} from 'lodash';
import ContentFilterList from 'material-ui/svg-icons/content/filter-list';
import * as React from 'react';
import {ThemeContext, withCssStyles} from '../../../components/hoc/withThemeProvider';
import {RowCenter} from '../../../components/layouts/row/Row';
import {translate} from '../../../services/translationService';
import {uuid} from '../../../types/Types';

export interface VisibilitySummaryProps {
  allMeters: uuid[];
  checkedMeters: uuid[];
}

export const VisibilitySummary = withCssStyles(
  ({checkedMeters, allMeters, cssStyles: {primary: {fg}}}: VisibilitySummaryProps & ThemeContext) => {
    const count = difference(allMeters, checkedMeters).length;
    return count > 0
      ? (
        <RowCenter style={{color: fg, margin: '16px 16px 0 16px'}}>
          {translate('{{count}} meters are not shown in the graph. Choose which meters you want to see with', {count})}
          <ContentFilterList color={fg} style={{marginLeft: 8}}/>
        </RowCenter>
      )
      : null;
  }
);
