import {difference} from 'lodash';
import ContentFilterList from 'material-ui/svg-icons/content/filter-list';
import * as React from 'react';
import {withContent} from '../../../components/hoc/withContent';
import {ThemeContext, withCssStyles} from '../../../components/hoc/withThemeProvider';
import {RowCenter} from '../../../components/layouts/row/Row';
import {translate} from '../../../services/translationService';
import {uuid} from '../../../types/Types';

export interface VisibilitySummaryProps {
  allMeters: uuid[];
  checkedMeters: uuid[];
}

interface ContentProps {
  fg: string;
  count: number;
}

type Props = VisibilitySummaryProps & ThemeContext;

const ContentComponent = withContent(({fg, count}: ContentProps) => (
  <RowCenter style={{color: fg, margin: '16px 16px 0 16px'}}>
    {translate('{{count}} meters are not shown in the graph. Choose which meters you want to see with', {count})}
    <ContentFilterList color={fg} style={{marginLeft: 8}}/>
  </RowCenter>
));

export const VisibilitySummary = withCssStyles(({checkedMeters, allMeters, cssStyles: {primary: {fg}}}: Props) => {
    const count = difference(allMeters, checkedMeters).length;
    return <ContentComponent hasContent={count > 0} count={count} fg={fg}/>;
  }
);
