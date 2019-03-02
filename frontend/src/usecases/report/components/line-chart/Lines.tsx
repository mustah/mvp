import * as React from 'react';
import {LabelProps, Line, YAxis} from 'recharts';
import {Children} from '../../../../types/Types';
import {GraphContents, LineProps} from '../../reportModels';

export const renderLines = (
  {lines, axes: {left, right}}: GraphContents,
  outerHiddenKeys,
  hiddenKeys,
  renderDot: (props) => React.ReactNode,
  renderActiveDot: (props) => React.ReactNode,
): Children[] => {

  const components: Children[] = lines
    .filter(line => hiddenKeys.findIndex((hiddenKey) => line.dataKey.startsWith(hiddenKey)) === -1)
    .filter(line => outerHiddenKeys.indexOf(line.id) === -1)
    .map((props: LineProps, index: number) => {
      const newDot = (apiDotProps) => renderDot({...apiDotProps, dataKey: props.dataKey});
      return (
        <Line
          animationDuration={600}
          key={index}
          type="monotone"
          connectNulls={true}
          {...props}
          activeDot={renderActiveDot}
          dot={newDot}
        />
      );
    });

  if (left) {
    const label: LabelProps = {value: left, position: 'insideLeft', angle: -90, dx: 10};
    components.push(<YAxis key="leftYAxis" label={label} yAxisId="left"/>);
  }

  if (right) {
    const label: LabelProps = {value: right, position: 'insideRight', angle: 90, dy: -10};
    components.push(<YAxis key="rightYAxis" label={label} yAxisId="right" orientation="right"/>);
  }

  return components;
};
