{{- define "orderflow.labels" -}}
app.kubernetes.io/part-of: orderflow
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end -}}

{{- define "orderflow.image" -}}
{{ .Values.image.registry }}/{{ .Values.image.project }}/{{ .Values.image.repository }}
{{- end -}}
