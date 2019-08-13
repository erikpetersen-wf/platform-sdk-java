// Package api provides JSON:APIv1.1 models for platform consumption.
// Read more at https://jsonapi.org/format/1.1/
package api

// ContentType is the JSON:API media type.
// https://jsonapi.org/format/1.1/#content-negotiation-all
const ContentType = `application/vnd.api+json`

// Version is the implemented standard version of JSONAPI.
const Version = `1.1`

// Details gives the expected details for a particular Document.
var Details = &JSONAPI{
	Version: Version,
}

// Document is the outermost JSON structure.
// https://jsonapi.org/format/1.1/#document-structure
type Document struct {
	Data     interface{} `json:"data,omitempty"` // []Resource or Resource
	Errors   []Error     `json:"errors,omitempty"`
	Meta     Meta        `json:"meta,omitempty"`
	JSONAPI  *JSONAPI    `json:"jsonapi,omitempty"`
	Links    Links       `json:"links,omitempty"`
	Included []Resource  `json:"included,omitempty"`
}

// Resource objects appear inn a JSON:API document to represent resources.
// https://jsonapi.org/format/1.1/#document-resource-objects
type Resource struct {
	ID    string      `json:"id"`
	Type  string      `json:"type"`
	Attrs interface{} `json:"attributes,omitempty"`
	// Rels  *Relationships `json:"relationships,omitempty"`
	Links Links `json:"links,omitempty"`
	Meta  Meta  `json:"meta,omitempty"`
}

// JSONAPI information about implementation of JSON:API.
// https://jsonapi.org/format/1.1/#document-jsonapi-object
type JSONAPI struct {
	Version string `json:"version"`
	Meta    Meta   `json:"meta,omitempty"`
}

// Meta includes non-standard meta-information.
// https://jsonapi.org/format/1.1/#document-meta
type Meta map[string]interface{}

// Links represents links to other documents of related resources.
// https://jsonapi.org/format/1.1/#document-links
// Values must be string or Link objects.
type Links map[string]interface{}

// Link represents links to other documents of related resources.
// https://jsonapi.org/format/1.1/#document-links
type Link struct {
	HREF string `json:"href,omitempty"`
	Meta Meta   `json:"meta,omitempty"`
}

// Error objects provide additional information about problems encounterd.
// https://jsonapi.org/format/1.1/#error-objects
type Error struct {
	ID     string `json:"id"`
	Links  Links  `json:"links,omitempty"`
	Status string `json:"status,omitempty"`
	Code   string `json:"code,omitempty"`
	Title  string `json:"title,omitempty"`
	Detail string `json:"detail,omitempty"`
	// Source interface{} `json:"source,omitempty"` // TODO
	Meta Meta `json:"meta,omitempty"`
}
